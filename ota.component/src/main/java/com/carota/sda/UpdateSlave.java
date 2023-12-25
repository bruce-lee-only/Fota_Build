/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.sda;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.agent.IRemoteAgent;
import com.carota.agent.RemoteAgent;
import com.carota.mda.remote.ActionDM;
import com.carota.mda.security.ISecuritySolution;
import com.carota.sda.util.RemoteAgentCallback;
import com.carota.sda.util.RemoteAgentServiceHolder;
import com.carota.sda.util.RemoteAgentServiceManager;
import com.carota.sda.util.SlaveEvent;
import com.momock.util.EncryptHelper;
import com.momock.util.JsonHelper;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public final class UpdateSlave implements ISlaveDownloadAgent, Runnable {

    public static final String INSTALL_KEY_MSG = "msg";
    public static final String INSTALL_KEY_NAME = "name";
    public static final String INSTALL_KEY_STATE = "state";
    public static final String INSTALL_KEY_DOMAIN = "domain";
    public static final String INSTALL_KEY_TV = "tv";
    public static final String INSTALL_KEY_ERROR = "code";

    public static final String INSTALL_KEY_DATA = "data";

    public static final String INSTALL_KEY_RETRY_NUM = "num";
    private static final int DEFAULT_DOMAIN = 0;

    public static final String INSTALL_MSG_TRANSPORT = "transport";
    public static final String INSTALL_MSG_VERIFY = "verify";
    public static final String INSTALL_MSG_DEPLOY = "deploy";
    public static final String INSTALL_MSG_INTERRUPT = "interrupt";
    public static final String INSTALL_MSG_REBOOT = "reboot";

    private final String mId;
    private final String mHost;
    private final String mAgent;
    private final String mPkg;
    private final long mResumeTimeout;
    private final ISecuritySolution mSolution;
    private final int mMaxRetry;
    private Context mContext;
    private File mDownloadDir;
    private SharedPreferences mPreferences;
    private SlaveTask mRunningTask;
    private SlaveState mState;
    private long mActiveTime;
    private SlaveEvent mSlaveEvent;
    private RemoteAgentServiceHolder mRemoteAgentHolder;
    private Thread mWorker;
    private ISlaveMethod mMethod;
    private int retry;

    private boolean isResume;

    public UpdateSlave(String slaveId, String slaveHost, String slaveAgent,
                       String pkg, long resumeTimeout, int retry,
                       ISlaveMethod method, ISecuritySolution secure) {
        mId = slaveId;
        mHost = slaveHost;
        mAgent = slaveAgent;
        mPkg = pkg;
        mResumeTimeout = resumeTimeout;
        mActiveTime = 0;
        mSlaveEvent = null;
        mRemoteAgentHolder = null;
        mWorker = null;
        mMethod = method;
        mSolution = secure;
        mMaxRetry = retry > 0 ? retry : 1;
        this.retry = 0;
    }

    @Override
    public void init(Context context, File workDir) {
        mContext = context;
        mDownloadDir = workDir;
        // load state from cache data
        mPreferences = context.getSharedPreferences(mId, Context.MODE_PRIVATE);
        mState = new SlaveState(mPreferences.getString(INSTALL_KEY_NAME, null), mPreferences.getInt(INSTALL_KEY_DOMAIN, DEFAULT_DOMAIN))
                .setState(mPreferences.getString(INSTALL_KEY_STATE, SlaveState.STATE_IDLE), mPreferences.getInt(INSTALL_KEY_ERROR, 0))
                .setMsg(mPreferences.getString(INSTALL_KEY_MSG, null));
        resetActiveTime();

        mSlaveEvent = SlaveEvent.getCache(context, mId);

        RemoteAgentServiceManager.get().add(context, mPkg, "ota.intent.action.BIND_RAS");
        mRemoteAgentHolder = RemoteAgentServiceManager.get().findAgentHolder(mPkg);
        if (mState.isRunning()) {
            try {
                String data = mPreferences.getString(INSTALL_KEY_DATA, null);
                retry = mPreferences.getInt(INSTALL_KEY_RETRY_NUM, 0);
                mRunningTask = TextUtils.isEmpty(data) ? null : SlaveTask.parseFrom(new JSONObject(data));
                Logger.debug("SlaveAgent RESUME %1d@ %2S", retry, mHost);
                isResume = true;
                triggerUpgradeInternal();
            } catch (Exception e) {
                Logger.error(e);
                mState.setState(SlaveState.STATE_ERROR, 99);
            }
        }
    }

    private IRemoteAgent getAgent() {
        return mRemoteAgentHolder.getAgent(mContext, mAgent);
    }

    @Override
    public String getId() {
        return mId;
    }

    @Override
    public String getHost() {
        return mHost;
    }

    @Override
    public SlaveInfo readInfo(String ecuName, Bundle bomInfo) {
        if (mState.name == null) mState.name = ecuName;
        return SlaveInfo.fromBundle(mMethod.readInfo(mContext, getAgent(), ecuName, bomInfo));
    }

    @Override
    public synchronized boolean triggerUpgrade(SlaveTask task) {
        if (null != mWorker) {
            // upgrade is already running
            return true;
        }
        // clean last before start
        mMethod.finishUpgrade(null, getAgent());
        // set state for Install Progress
        retry = 0;
        mRunningTask = task;
        mState = new SlaveState(task.name, task.domain);
        SharedPreferences.Editor editor = mPreferences.edit().clear()
                .putString(INSTALL_KEY_NAME, task.name)
                .putString(INSTALL_KEY_STATE, SlaveState.STATE_DOWNLOAD)
                .putString(INSTALL_KEY_MSG, null)
                .putInt(INSTALL_KEY_ERROR, 0)
                .putInt(INSTALL_KEY_DOMAIN, task.domain)
                .putString(INSTALL_KEY_TV, task.targetVer);
        try {
            JSONObject parse = JsonHelper.parse(SlaveTask.toBundle(task));
            editor.putString(INSTALL_KEY_DATA, parse.toString());
        } catch (Exception e) {
            Logger.error(e);
        }
        mState.setState(SlaveState.STATE_DOWNLOAD, 0);
        editor.commit();
        isResume = false;
        // init parameter for upgrade
        return triggerUpgradeInternal();
    }

    private synchronized boolean triggerUpgradeInternal() {
        // init parameter for upgrade
        resetActiveTime();
        mWorker = new Thread(this);
        mWorker.start();
        return true;
    }

    @Override
    public synchronized SlaveState queryState(String ecuName) {
        if (mState.name == null) mState.name = ecuName;
        return mState;
    }

    @Override
    public boolean isRunning() {
        return mState.isRunning();
    }

    @Override
    public boolean fetchEvent(String type, int max, List<String> events, List<String> ids) {
        return mSlaveEvent.fetchEvent(type, max, events, ids);
    }

    @Override
    public boolean deleteEvent(List<String> ids) {
        return mSlaveEvent.deleteEvent(ids);
    }

    @Override
    public boolean listLogFiles(String type, int max, List<String> fileNames, String extraPath) {
        IRemoteAgent agent = getAgent();
        try {
            Bundle bundle = new Bundle();
            if (extraPath != null) {
                bundle.putString("extra_path", extraPath);
            }
            if (null != agent && agent.archiveLogs(type, bundle)) {
                File dir = RemoteAgentCallback.getLogCacheDir(mContext, mId);
                for (File f : dir.listFiles()) {
                    fileNames.add(f.getName());
                }
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public File findLogFile(String name) {
        File dir = RemoteAgentCallback.getLogCacheDir(mContext, mId);
        return new File(dir, name);
    }

    @Override
    public void run() {
        try {
            while (mState.isRunning()) {
                try {
                    if (isResume) {
                        Logger.debug("SlaveAgent Start Wait Result @ %1s", mHost);
                        waitUpgradeFinished();
                        Logger.error("SlaveAgent Wait Result End ,Result:%1s Msg:%2s Pro:%3d @ %4s", mState.getState(), mState.getMsg(), mState.getProgress(), mHost);
                    } else {
                        isResume = true;
                    }
                    if (mState.isRunning()) startUpgrade();
                } catch (InterruptedException ignored) {
                }
            }
        } catch (TimeoutException e) {
            Logger.error(e);
            setAgentState(SlaveState.STATE_FAILURE, null, 98);
        } catch (Exception e) {
            Logger.error(e);
            setAgentState(SlaveState.STATE_ERROR, null, 99);
        } finally {
            mMethod.finishUpgrade(null, getAgent());
            mWorker = null;
        }
    }

    private void startUpgrade() throws Exception {
        if (mMaxRetry <= retry) {
            Logger.debug("SlaveAgent Install Max Retry @ " + mHost);
            setAgentState(SlaveState.STATE_FAILURE, null, 100);
            return;
        } else {
            retry++;
        }
        Logger.debug("SlaveAgent START :%1d @ %2s", retry, mHost);
        mPreferences.edit().putInt(INSTALL_KEY_RETRY_NUM, retry).commit();
        mState.setState(SlaveState.STATE_UPGRADE, 0);
        // search target file in DM and RSM
        File target = new File(mDownloadDir, mRunningTask.targetId);
        if (!target.exists()) {
            InputStream inputStream = ActionDM.openInputStream(mRunningTask.hostDM, mRunningTask.targetId);
            if (inputStream == null) {
                Logger.error("SlaveAgent Missing Target File @ %s", mHost);
                throw new FileNotFoundException("Missing Target File");
            }
            if (!mRunningTask.targetId.equals(EncryptHelper.calcFileMd5(inputStream))) {
                Logger.error("SlaveAgent Missing Target File @ %s", mHost);
                inputStream.close();
                throw new FileNotFoundException("Missing Target File");
            }
            inputStream.close();
        }
        Logger.debug("SlaveAgent Install @ " + mHost);
        Bundle extraData = SlaveTask.toBundle(mRunningTask);
        extraData.putLong("timeout", mResumeTimeout);
        if (mRunningTask.applyInfo != null && mRunningTask.applyInfo.length > 0) {
            extraData.putBoolean("is_sub", true);
        }

        AtomicBoolean verifyResult = new AtomicBoolean(false);
        Logger.debug("SlaveAgent Verify @ " + mHost);
        if (null != mSolution) {
            File signFile = new File(mDownloadDir, mRunningTask.targetSign);
            if (target.exists()) {
                target = mSolution.decryptPackage(verifyResult, target, mDownloadDir, signFile);
                Logger.debug("SlaveAgent Decrypt @ " + (null != target) + " - " + verifyResult.get());
                if (null == target) {
                    throw new RuntimeException("Target Fail to Verify @ " + mHost);
                }
            } else {
                if (!signFile.exists()) {
                    signFile = new File(mDownloadDir, "/sign/" + mRunningTask.targetSign);
                }
                Boolean decryptPackage = mSolution.decryptPackage(verifyResult, mRunningTask.hostDM, mRunningTask.targetId, signFile);
                if (!decryptPackage) {
                    throw new RuntimeException("Target Stream Fail to Verify @ " + mHost);
                }
            }
        } else {
            // verify is disabled by RAS configure
            verifyResult.set(true);
        }
        if (!verifyResult.get()) {
            throw new RuntimeException("Fail to Verify");
        }
        if (mMethod.startUpgrade(mContext, target, getAgent(), extraData)) {
            Logger.debug("SlaveAgent Func Success @ " + mHost);
            // add gap before query result
            if (mRunningTask.domain < 0) {
                Thread.sleep(5000);
                Logger.debug("SlaveAgent Wait Reboot");
                synchronized (mAgent) {
                    do {
                        mAgent.wait();
                        Logger.error("SlaveAgent Wait Loop");
                        Thread.sleep(10000);
                    } while (true);
                }
            }
        } else {
            Logger.error("SlaveAgent Func FAIL @ " + mHost);
        }

    }

    private void waitUpgradeFinished() throws InterruptedException, TimeoutException {
        do {
            Thread.sleep(10_000);
            if (mState.name == null) {
                Logger.error("SlaveAgent Wait Name @%1s", mHost);
                continue;
            }
            Bundle data = mMethod.queryStatus(mContext, getAgent(), mState.name);
            int pro = data.getInt(RemoteAgent.KEY_STATUS_PROGRESS);
            mState.setProgress(pro);
            int ret = data.getInt(RemoteAgent.KEY_STATUS_RESULT, Integer.MIN_VALUE);
            String msg = data.getString(INSTALL_KEY_MSG, null);
            int ec = data.getInt(RemoteAgent.KEY_ERROR_CODE, 0);
            String state = null;
            switch (ret) {
                case RemoteAgent.INSTALL_ERROR_UNKNOWN:
                    state = SlaveState.STATE_ERROR;
                    msg = INSTALL_MSG_INTERRUPT;
                    break;
                case RemoteAgent.INSTALL_ERROR_UPGRADE:
                    state = SlaveState.STATE_FAILURE;
                    msg = INSTALL_MSG_DEPLOY;
                    break;
                case RemoteAgent.INSTALL_ERROR_UPLOAD:
                case RemoteAgent.INSTALL_ERROR_DOWNLOAD:
                    state = SlaveState.STATE_DOWNLOAD;
                    msg = INSTALL_MSG_TRANSPORT;
                    break;
                case RemoteAgent.INSTALL_ERROR_VERIFY:
                    state = SlaveState.STATE_ERROR;
                    msg = INSTALL_MSG_VERIFY;
                    break;
                case RemoteAgent.INSTALL_SUCCESS:
                    state = SlaveState.STATE_SUCCESS;
                    msg = INSTALL_MSG_REBOOT;
                    break;
            }

            if (data.getBoolean(RemoteAgent.KEY_STATUS_TRIGGERED)) {
                mState.setMsg(INSTALL_MSG_REBOOT);
            } else {
                mState.setMsg(msg);
            }
            if (state != null && !SlaveState.STATE_SUCCESS.equals(state)) {
                //upgrade fail
                if (isEndWaitResult()) {
                    Logger.error("SlaveAgent Func FAIL ,Msg %1s @ %2s", msg, mHost);
                    return;
                }
            }

            if (null != state) {
                setAgentState(state, msg, ec);
            } else if (getUsedTime() > mResumeTimeout) {
                // Set error when resume timeout.
                throw new TimeoutException("SlaveAgent TimeOut");
            } else {
                continue;
            }
            break;
        } while (mState.isRunning());
    }

    /**
     * @return true when can retry
     */
    private boolean isEndWaitResult() {
        if (mRunningTask == null) {
            return false;
        }
        String data = mPreferences.getString(INSTALL_KEY_DATA, null);
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        if (mMaxRetry > retry) {
            return false;
        }
        try {
            JSONObject object = new JSONObject(data);
            SlaveTask task = SlaveTask.parseFrom(object);
            return !TextUtils.isEmpty(task.targetId);
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    private synchronized void setAgentState(String state, String msg, int errorCode) {
        if (null != mState) {
            mState.setMsg(msg);
            mState.setState(state, errorCode);
        }
        mPreferences.edit().putString(INSTALL_KEY_STATE, state)
                .putString(INSTALL_KEY_MSG, msg)
                .putInt(INSTALL_KEY_ERROR, errorCode)
                .commit();
    }

    private void resetActiveTime() {
        mActiveTime = SystemClock.elapsedRealtime();
    }

    private long getUsedTime() {
        return SystemClock.elapsedRealtime() - mActiveTime;
    }
}
