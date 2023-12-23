/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.agent;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class RemoteAgent {

    private static class RemoteAgentBinder extends IRemoteAgent.Stub {

        private RemoteAgent mAgent;

        private RemoteAgentBinder(RemoteAgent agent) {
            mAgent = agent;
        }

        @Override
        public boolean registerCallback(IRemoteAgentCallback callback) throws RemoteException {
            return mAgent.registerCallback(callback);
        }

        @Override
        public Bundle queryInfo(int flag, String ecuName, Bundle bomInfo) throws RemoteException {
            return mAgent.queryInfo(flag, ecuName, bomInfo);
        }

        @Override
        public int triggerUpgrade(ParcelFileDescriptor data, Bundle extra) throws RemoteException {
            return mAgent.triggerUpgrade(data, extra);
        }

        @Override
        public boolean archiveLogs(String type, Bundle extra) throws RemoteException {
            return mAgent.archiveLogs(type, extra);
        }

        @Override
        public boolean finishUpgrade(Bundle extra) throws RemoteException {
            mAgent.finishUpgrade(extra);
            return true;
        }
    }

    public static final int INSTALL_ERROR_UNKNOWN = -5;
    public static final int INSTALL_ERROR_UPGRADE = -4;
    public static final int INSTALL_ERROR_UPLOAD = -3;
    public static final int INSTALL_ERROR_VERIFY = -2;
    public static final int INSTALL_ERROR_DOWNLOAD = -1;
    public static final int INSTALL_SUCCESS = 0;
    public static final int INSTALL_WAIT = 1;

    public static final int FLAG_PROP = 0x00000001;
    public static final int FLAG_STATUS = 0x00000002;
    public static final int FLAG_APP = 0x00000004;
    public static final int FLAG_SDK = 0x00000008;

    public static final String KEY_APPID = "app_id";
    public static final String KEY_APP_VER = "app_v";
    public static final String KEY_APP_VER_CODE = "app_vc";
    public static final String KEY_SOFTWARE_VER = "sv";
    public static final String KEY_HARDWARE_VER = "hv";
    public static final String KEY_SERIAL_NUMBER = "sn";
    public static final String KEY_SECURE_ID = "security";
    public static final String KEY_UA_VER = "uav";
    public static final String KEY_STATUS_PROGRESS = "progress";
    public static final String KEY_STATUS_TRIGGERED = "trig";
    public static final String KEY_ERROR_CODE = "ec";
    public static final String KEY_BOM = "bom";
    /**
     * WARNING:
     * KEY_STATUS_RESULT MUST NOT be set when installing
     * For internal use only.
     */
    public static final String KEY_STATUS_RESULT = "result";

    public final String NAME;
    private IRemoteAgentCallback mCallback;
    private Context mContext;
    private File mSpec;
    private boolean mIsRunning;


    /**
     * @param name   name of device(assign by OTA)
     */
    public RemoteAgent(Context context, String name) {
        NAME = name;
        mContext = context.getApplicationContext();
        mSpec = new File(mContext.getFilesDir(), "ra-spec-" + name);
        mIsRunning = false;
    }

    final RemoteAgentBinder buildBinder() {
        return new RemoteAgentBinder(this);
    }

    private boolean registerCallback(IRemoteAgentCallback callback) throws RemoteException {
        mCallback = callback;
        return null != callback;
    }

    private Bundle queryInfo(int flag, String name, Bundle bomInfo) throws RemoteException {
        Bundle ret = new Bundle();
        ret.putString("id", NAME);
//        ret.putBundle(KEY_BOM, bomInfo);

        if(0 != (flag & FLAG_SDK)) {
            ret.putString("sdk_v", BuildConfig.VERSION_NAME);
            ret.putInt("sdk_vc", BuildConfig.VERSION_CODE);
        }

        if (0 != (flag & FLAG_STATUS)) {
            ret.putBoolean(KEY_STATUS_TRIGGERED, isUpgradeTriggered(null));
            if (!mIsRunning) {
                AgentState result = queryInstallResult(name);
                if (result == null) {
                    throw new NullPointerException("queryInstallResult() return null");
                }
                ret.putInt(KEY_STATUS_PROGRESS, result.getProgress());
                ret.putInt(RemoteAgent.KEY_STATUS_RESULT, result.getResult());
                ret.putInt(RemoteAgent.KEY_ERROR_CODE, result.getErrorCode());
                if (result.getExtra()!=null) {
                    ret.putAll(result.getExtra());
                }
            }
        }

        if(0 != (flag & FLAG_APP)) {
            onGetAppInfo(ret);
        }

        if(0 != (flag & FLAG_PROP)) {
            onGetProp(ret);
        }
        return ret;
    }
    private synchronized int triggerUpgrade(ParcelFileDescriptor data, Bundle extra) {
        mIsRunning = true;
        if (extra.getBoolean("is_sub", false)) {
            String name = extra.getString("tgt_id", null);
            try {
                if (triggerInstall(name, null, extra, isUpgradeTriggered(name))) {
                    return INSTALL_SUCCESS;
                }
            } catch (IOException e) {
                // do nothing
            } finally {
                mIsRunning = false;
            }
        } else {
            try (ParcelFileDescriptor.AutoCloseInputStream acis = new ParcelFileDescriptor.AutoCloseInputStream(data)) {
                String name = extra.getString("tgt_id", null);
                if (triggerInstall(name, acis, extra, isUpgradeTriggered(name))) {
                    return INSTALL_SUCCESS;
                }
            } catch (Exception e) {
                // do nothing
            } finally {
                mIsRunning = false;
            }
        }
        return INSTALL_ERROR_UNKNOWN;
    }

    private boolean archiveLogs(String type, Bundle extra) throws RemoteException {
        if(null != mCallback) {
            try {
                File logFile = packageLogFiles(type, extra);
                if(null != logFile) {
                    try (ParcelFileDescriptor pfd = ParcelFileDescriptor.open(logFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
                        return mCallback.onLogArchived(NAME, type, extra, pfd);
                    } finally {
                        logFile.delete();
                    }
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        return false;
    }

    private void finishUpgrade(Bundle extra) throws RemoteException {
        mSpec.delete();
        onFinishUpgrade(extra);
    }

    protected void onFinishUpgrade(Bundle extra) {
    }

    final public void setUpgradeTriggered(String descriptor) {
        try {
            if(TextUtils.isEmpty(descriptor)){
                mSpec.delete();
            }else{
                mSpec.getParentFile().mkdirs();
                FileHelper.writeTextImmediately(mSpec, descriptor, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    final public boolean isUpgradeTriggered(String descriptor) {
        String desc = null;
        try {
            desc = FileHelper.readText(mSpec, null);
        } catch (IOException e) {
            // do nothing
        }
        return null != desc && (null == descriptor || desc.equals(descriptor));
    }

    final protected Context getContext() {
        return mContext;
    }

    private void onGetAppInfo(Bundle ret) {
        try{
            String appId = mContext.getPackageName();
            PackageInfo pi = mContext.getPackageManager().getPackageInfo(appId, 0);
            ret.putString(KEY_APPID, appId);
            ret.putString(KEY_APP_VER, pi.versionName);
            ret.putInt(KEY_APP_VER_CODE, pi.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onGetProp(Bundle ret) {
        ret.putString(KEY_SECURE_ID, getSecureId());
        String sv = getSoftwareVersion(ret.getBundle(KEY_BOM));
        if (TextUtils.isEmpty(sv)) {
            return;
        }
        ret.putString(KEY_SOFTWARE_VER, sv);
        String hv = getHardwareVersion(ret.getBundle(KEY_BOM));
        if (TextUtils.isEmpty(hv)) {
            return;
        }
        ret.putString(KEY_HARDWARE_VER, hv);
        String sn = getSerialNumber(ret.getBundle(KEY_BOM));
        if (TextUtils.isEmpty(sn)) {
            return;
        }
        ret.putString(KEY_SERIAL_NUMBER, sn);
        Bundle extra = getExtraInfo(ret.getBundle(KEY_BOM));
        if(null != extra) {
            for(String key : extra.keySet()) {
                if(ret.containsKey(key)) {
                    extra.remove(key);
                }
            }
            ret.putAll(extra);
        }
    }

    public abstract String getSoftwareVersion(Bundle bomInfo);

    public abstract String getHardwareVersion(Bundle bomInfo);

    public abstract String getSerialNumber(Bundle bomInfo);

    public String getSecureId () {
        return null;
    }

    public Bundle getExtraInfo(Bundle bomInfo) {
        return null;
    }

    /**
     * @return install function result
     */
    public abstract AgentState queryInstallResult(String ecuName);

    /**
     * Called by the OTA when tiggerUpgrade called.  Do not call this method directly.
     * Implement of Real Install function with follow steps
     * 1. copy package to private storage (if needed)
     * 2. upload file
     * 3. start install(if possible)
     * 4. wait install finished(if possible)
     * @param descriptor task ID
     * @param pkg  upgrade file
     * @param extra   Reserved
     * @return  trigger result
     */
    protected abstract boolean triggerInstall(String descriptor,
                                           InputStream pkg, Bundle extra,
                                           boolean isTriggered) throws IOException;


    /**
     * This is a part of Diagnosis Function. collect log files.
     * 1. find available log files by type
     * 2. zip file into one
     * @param type  log type, null means ALL
     * @param extra  Reserved
     * @return  null-not support or error;
     *          object-zipped log file
     */
    public File packageLogFiles(String type, Bundle extra) {
        return null;
    }


    /**
     * This is a part of Diagnosis Function, collect custom event.
     * @param type    log type
     * @param event   event describe
     * @param extra   Reserved
     * @return   true-recorded and wait to send
     *           false-fail to record
     */
    final public boolean logCustomEvent(String type, String event, Bundle extra) {
        if(null != mCallback) {
            try {
                mCallback.onEventProcess(NAME, type, event, extra);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static String getSlaveCacheName(String name) {
        return "sda-" + name;
    }
}
