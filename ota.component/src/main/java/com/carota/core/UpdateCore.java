/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core;

import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

import com.carota.build.IConfiguration;
import com.carota.build.ParamHub;
import com.carota.build.ParamMDA;
import com.carota.core.data.CoreStatus;
import com.carota.core.data.DataCache;
import com.carota.core.data.UpdateSession;
import com.carota.core.monitor.DownloadMonitor;
import com.carota.core.monitor.InstallEventHandler;
import com.carota.core.monitor.InstallMonitor;
import com.carota.core.remote.ActionMDA;
import com.carota.core.remote.IActionMDA;
import com.carota.core.remote.info.MDAInfo;
import com.carota.html.HtmlHelper;
import com.carota.svr.PrivReqHelper;
import com.carota.util.ConfigHelper;
import com.momock.util.Logger;
import com.momock.util.SystemHelper;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpdateCore {

    private IActionMDA mActionMDA;
    private InstallMonitor mInsMonitor;
    private DownloadMonitor mDownloadMonitor;
    private DataCache mCache;
    private CoreStatus mCoreStatus;
    private UpdateSession mSession;
    private final AtomicBoolean mMainCtrlReady;
    private Context mContext;
    private boolean isUat;

    public UpdateCore(Context context) {
        Context ctx = context.getApplicationContext();
        mContext = ctx;
        mMainCtrlReady = new AtomicBoolean(false);
        IConfiguration cfg = ConfigHelper.get(ctx);
        mCache = new DataCache(ctx);
        mCoreStatus = new CoreStatus(ctx);
        mActionMDA = createActionMDA(cfg);
        mDownloadMonitor = new DownloadMonitor(mActionMDA, mCoreStatus);
        ParamMDA paramMDA = cfg.get(ParamMDA.class);
        mInsMonitor = new InstallMonitor(mActionMDA, paramMDA.getTimeout());
        UpdateSession session = new UpdateSession(mCache.getConnData());
        mSession = session.check() ? session : null;
    }

    public static IActionMDA createActionMDA(IConfiguration cfg) {
        ParamMDA paramMDA = cfg.get(ParamMDA.class);
        ParamHub paramHub = cfg.get(ParamHub.class);

        String proxyAddr = paramHub.getAddr();
        Logger.debug("HU Client set Proxy : " + proxyAddr);
        if(TextUtils.isEmpty(proxyAddr)) {
            Logger.info("HU Client Mode : DIRECT");
            return new ActionMDA(paramMDA.getAddr(), paramMDA.getPort());
        } else {
            Logger.info("HU Client Mode : PROXY");
            PrivReqHelper.setGlobalProxy(proxyAddr, paramHub.getPort());
            return new ActionMDA(paramMDA.getHost(), 0);
        }
    }

    public boolean syncDataFromMaster() {
        waitMainCtrlReady("SYNC");
        Logger.debug("HuClient SYNC-MDA");
        MDAInfo info;
        do {
            info = mActionMDA.syncMasterStatus();
        } while (info == null);
        isUat = info.isUat();
        if(info.checkStatus(MDAInfo.FLAG_DOWNLOADING)) {
            mDownloadMonitor.pauseDownload(mSession);
        }

        boolean downloaded = info.checkStatus(MDAInfo.FLAG_DOWNLOADED);
        boolean upgrade = info.checkStatus(MDAInfo.FLAG_INSTALLING);

        if(downloaded || upgrade ) {
            UpdateSession us = mActionMDA.connect(IActionMDA.CONN_ACTION_SYNC, null);
            if(null != us && us.check()) {
                mCache.setConnData(us.getRawData());
                mCache.setVehicleInfo(us.getVinCode());
                mCoreStatus.reset(us.getUSID(), us.getTaskCount());
                mCoreStatus.setPackageReady(true);
                mSession = us;
                Logger.debug("HuClient SYNC-MDA Resume");
            } else {
                mCache.setConnData(null);
                mSession = null;
                upgrade = false;
                Logger.error("HuClient SYNC-MDA Failure");
            }
        } else {
            // no update is active
            mCache.setConnData(null);
            mSession = null;
            Logger.debug("HuClient SYNC-MDA Idle");
        }
        return upgrade;
    }

    public void waitMainCtrlReady(String key) {
        synchronized (mMainCtrlReady) {
            if (!mMainCtrlReady.get()) {
                try {
                    // boost init
                    Thread.sleep(2000);
                    while (!mActionMDA.checkAlive()) {
                        Logger.debug("HuClient WAIT-MDA @ " + key);
                        Thread.sleep(10 * 1000);
                    }
                    mMainCtrlReady.set(true);
                } catch (Exception e) {
                    throw new RuntimeException("Interrupted @ Wait Master");
                }
            }
        }
    }

    public boolean waitRemoteSystemReady(long timeoutSecond, List<String> lostEcus) {
        long endTime = SystemClock.elapsedRealtime() + timeoutSecond;
        try {
            while (!mActionMDA.checkSystemReady(lostEcus)) {
                if(SystemClock.elapsedRealtime() > endTime) {
                    Logger.debug("HuClient WAIT-SDA timeout");
                    return false;
                }
                Logger.debug("HuClient WAIT-SDA");
                Thread.sleep(10 * 1000);
            }
            return true;
        } catch (InterruptedException ie) {
            // interrupted
        }
        return false;
    }

    public ISession check(Bundle props, ICheckCallback callback) {
        if(mInsMonitor.isRunning()) {
            Logger.debug("HuClient CONN RUN");
            if(null != callback) {
                callback.onError("CONN RUN", 0);
            }
        } else {
            mDownloadMonitor.pauseDownload(mSession);
            UpdateSession us = mActionMDA.connect(IActionMDA.CONN_ACTION_CHECK, props);
            if (null != us) {
                Logger.debug("HuClient CONN OK : %d", us.getTaskCount());
                //add by wangjian for dtc start
//                VehicleDTC.get(mContext).setVinCode(us.getVinCode());
                //add by wangjian for dtc end
                if(null != callback) {
                    callback.onConnected(us);
                }
                if(us.check()) {
                    HtmlHelper.downloadHtml(mContext, us.getDisplayInfoUrl(),us.getScheduleID());
                    mCache.setConnData(us.getRawData());
                    mCache.setVehicleInfo(us.getVinCode());
                    mCoreStatus.reset(us.getUSID(), us.getTaskCount());
                    mSession = us;
                    return mSession;
                }
            } else {
                Logger.debug("HuClient CONN FAIL");
                if(null != callback) {
                    callback.onError("CONN FAIL", -1);
                }
            }
            mSession = null;
        }
        return null;
    }

    public boolean scheduleDownload(IDownloadCallback callback) {
        return mDownloadMonitor.startDownload(mSession, callback);
    }

    public boolean stopDownload() {
        return mDownloadMonitor.pauseDownload(mSession);
    }

    public boolean isSessionExpired(boolean ignoreExpireConfirmFail) {
        UpdateSession us = mActionMDA.connect(IActionMDA.CONN_ACTION_VERIFY, null);
        return null == us || !(us.isFresh() || ignoreExpireConfirmFail);
    }

    public boolean install(IInstallViewHandler view, boolean ignoreExpireConfirmFail) {
        if (null != mSession && view.onInstallStart(mSession)) {
            if(isSessionExpired(ignoreExpireConfirmFail)) {
                view.onInstallStop(mSession, ClientState.UPGRADE_STATE_IDLE);
                return true;
            }
            return mInsMonitor.triggerUpgrade(false,
                    mSession, new InstallEventHandler(mCoreStatus, view),mContext);
        }
        return false;
    }

    public boolean resumeInstall(IInstallViewHandler view) {
        if(null != mSession && view.onInstallStart(mSession)) {
            Logger.debug("HuClient Resume Install @ REBOOT");
            waitMainCtrlReady("Re-INS");
            mInsMonitor.triggerUpgrade(true, mSession, new InstallEventHandler(mCoreStatus, view),mContext);
            return true;
        }
        return false;
    }

    public ISession getCurSession() {
        return mSession;
    }

    public ICoreStatus getCurState() {
        return mCoreStatus;
    }

    public boolean sendUiPoint(int type,long time,String msg) {
        return mActionMDA.sendPointData(type, time, msg);
    }

    public boolean sendUiEvent(long time, int upgradeType, int code, String msg, int result) {
        if (mSession == null) {
            return false;
        }
        boolean isEIC = mSession.getOperation().contains(ISession.OPERATE_EIC_OFF);
        return mActionMDA.sendEventData(time, upgradeType, code, msg, result,mSession.getScheduleID(),isEIC? 1:0);
    }

    public boolean sendFotaV2Data(String ecu, int state, int ecuState, int code, String errorMsg, long time) {
        return mActionMDA.sendFotaV2Data(ecu, state,ecuState,code,errorMsg, time);
    }

    public boolean getMDAEnvm() {
        return isUat;
    }

    public boolean setMDAEnvm(boolean isUat) {
        if (mActionMDA.setMasterEnvm(isUat)) {
            SystemHelper.execScript("pm clear " + mContext.getPackageName());
            SystemHelper.execScript("reboot");
        }
        return false;
    }
}
