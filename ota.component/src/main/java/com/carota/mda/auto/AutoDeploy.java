/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.auto;

import android.content.Context;
import android.text.TextUtils;

import com.carota.CarotaClient;
import com.carota.build.IConfiguration;
import com.carota.build.ParamDM;
import com.carota.build.ParamMDA;
import com.carota.build.ParamRoute;
import com.carota.core.ISession;
import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.data.UpdateItem;
import com.carota.mda.deploy.DeviceUpdater;
import com.carota.mda.download.DownloadCtrl;
import com.carota.mda.remote.ActionSDA;
import com.carota.mda.remote.info.MetaInfo;
import com.carota.mda.remote.info.SlaveInstallResult;
import com.carota.mda.security.SecurityCenter;
import com.carota.mda.security.SecurityData;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.mda.telemetry.FotaState;
import com.carota.util.ConfigHelper;
import com.carota.util.SecurityUtil;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

import java.io.File;


public class AutoDeploy {

    private AutoDataCache mDataCache;
    private IConfiguration mConfigure;
    private FotaAnalytics mAnalytics;
    private SecurityCenter mSecure;
    private SerialExecutor mExecutor;
    private File mDownloadDir;

    public AutoDeploy(Context context, FotaAnalytics analyze, SecurityCenter security) {
        this.mAnalytics = analyze;
        this.mSecure = security;
        mDataCache = new AutoDataCache(context);
        mConfigure = ConfigHelper.get(context);
        mExecutor = new SerialExecutor();
        mDownloadDir = ConfigHelper.get(context).get(ParamDM.class).getDownloadDir(context);
    }

    public boolean process(final UpdateCampaign session) {
        if (!ISession.MODE_AUTO_INSTALL_SILENT.equals(session.getMode())
                // for compatibility
                && !"auto-install".equals(session.getMode())) {
            mDataCache.clear();
            return false;
        }

        Logger.info("AUTO Work Received");
        if(!mExecutor.isRunning()) {
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Logger.info("AUTO Work Start");
                        CarotaClient.waitBootComplete("INS-AD");
                        mDataCache.setSession(session);
                        if(mDataCache.getRetryCount() >= 3) {
                            Logger.info("AUTO Work Max Retry");
                            return;
                        }
                        if(session.getItemCount() > 0) {
                            doWork(session);
                        } else {
                            Logger.info("AUTO Work Empty");
                        }
                    } catch (Exception e) {
                        Logger.error(e);
                    } finally {
                        mDataCache.setRunning(false);
                    }
                    Logger.info("AUTO Work End : r = " + mDataCache.getRetryCount());
                }
            });
        } else {
            Logger.debug("AUTO Work Busy");
        }
        return true;
    }

    // call after reboot, check update result
    public boolean resume() {
        if (mDataCache.isRunning()) {
            process(mDataCache.getSession());
            return true;
        }
        return false;
    }

    public boolean isRunning() {
        return mDataCache.isRunning();
    }

    private synchronized void doWork(UpdateCampaign us) throws InterruptedException {
        ParamMDA paramMDA = mConfigure.get(ParamMDA.class);
        ParamRoute paramRoute = mConfigure.get(ParamRoute.class);

        DownloadCtrl downloadCtrl = new DownloadCtrl(mSecure, paramMDA, paramRoute);
        downloadCtrl.reset(us, new AutoDownloadEventHandler(mAnalytics, us, mSecure));
        downloadCtrl.start();
        do {
            Thread.sleep(5 * 1000);
            Logger.info("AUTO Download Wait...");
        } while (downloadCtrl.isRunning());

        if (!downloadCtrl.isFinished()) {
            Logger.error("AUTO Download Interrupted");
            return;
        }

        boolean resume = mDataCache.setRunning(true);
        Logger.error("AUTO Cache R : " + resume);
        UpdateItem task = us.getItem(0);
        String name = task.getProp(UpdateItem.PROP_NAME);
        String ecuHost = ParamRoute.getEcuHost(paramRoute.getRoute(name));
        if (TextUtils.isEmpty(ecuHost)) ecuHost = paramRoute.getSubHost();
        String dmHost = downloadCtrl.findDownloaderHost(name);
        Logger.debug("AUTO Updater : " + name + "[" + dmHost + "] : " + paramMDA.getTimeout());


        SecurityData tgtSec;
        String tgtSignId = null;
        if (task.getProp(UpdateItem.PROP_HAS_SECURITY, Boolean.FALSE) && paramMDA.isSecureEnabled(task.getProp(UpdateItem.PROP_NAME))) {
            Logger.info("DeviceUpdater @ SEC ON");
            tgtSec = mSecure.load(task.getProp(UpdateItem.PROP_DST_MD5));
            if (null == tgtSec) {
                Logger.error("DeviceUpdater @ Missing SEC TGT");
                return;
            }
            tgtSignId = SecurityUtil.findSignFile(tgtSec.md5, mDownloadDir, dmHost, MetaInfo.PROP_DST);
            if (!mSecure.verifyPackage(dmHost, task.getProp(UpdateItem.PROP_DST_MD5), tgtSignId)) {
                Logger.error("DeviceUpdater @ Fail to Verify TGT");
                return;
            }
        } else {
            Logger.info("Updater @ SEC OFF");
        }

        DeviceUpdater updater = new DeviceUpdater(new ActionSDA(), paramMDA.getTimeout(), us.getBomInfo(name));
        updater.setResume(resume);
        updater.setDevice(ecuHost, name, dmHost, -1);
        updater.setTarget(task.getProp(UpdateItem.PROP_DST_MD5), task.getProp(UpdateItem.PROP_DST_VER), tgtSignId);

        int result = updater.call();

        addFakeLogicReport(us.getUSID(), name, updater.getStep(), task.getProp(UpdateItem.PROP_HAS_SECURITY, Boolean.FALSE));

        // report install result
        int state = FotaAnalytics.OTA.STATE_FAIL;
        boolean increaseRetryCount = true;
        if(DeviceUpdater.RET_SUCCESS == result) {
            state = FotaAnalytics.OTA.STATE_SUCCESS;
            increaseRetryCount = false;
        }
        mAnalytics.logUpgradeStateV2(us.getUSID(), name, state, state == FotaAnalytics.OTA.STATE_FAIL ? FotaState.OTA.STATE_UPDATE_FAILURE : FotaState.OTA.STATE_UPGRADE,
                updater.getErrorCode(), "");
        mDataCache.resetTriggered(increaseRetryCount);
    }

    private void addFakeLogicReport(String usid, String name, int errorCode, boolean secure) {
        int target = FotaAnalytics.OTA.TARGET_UPGRADE_DST;

        if (SlaveInstallResult.STEP_NONE == errorCode || SlaveInstallResult.STEP_DEPLOY == errorCode) {
            mAnalytics.logUpgradeStateV2(usid, name, FotaState.OTA.STATE_UPGRADE, FotaState.OTA.STATE_UPGRADE, FotaState.OTA.UPGRADE.CODE_INSTALL_VERIFY_MD5, "");
            if (secure) {
                mAnalytics.logUpgradeStateV2(usid, name, FotaState.OTA.STATE_UPGRADE, FotaState.OTA.STATE_UPGRADE, FotaState.OTA.UPGRADE.CODE_INSTALL_VERIFY_PKI, "");
            }
        } else if (SlaveInstallResult.STEP_TRANSPORT == errorCode) {
            mAnalytics.logUpgradeStateV2(usid, name, FotaState.OTA.STATE_UPDATE_FAILURE, FotaState.OTA.STATE_UPDATE_FAILURE,FotaState.OTA.FAILURE.CODE_INSTALL_VERIFY_MD5,"");
        } else if (SlaveInstallResult.STEP_VERIFY == errorCode) {
            mAnalytics.logUpgradeStateV2(usid, name, FotaState.OTA.STATE_UPDATE_FAILURE, FotaState.OTA.STATE_UPDATE_FAILURE, FotaState.OTA.FAILURE.CODE_INSTALL_VERIFY_MD5, "");
            mAnalytics.logUpgradeStateV2(usid, name, FotaState.OTA.STATE_UPDATE_FAILURE, FotaState.OTA.STATE_UPDATE_FAILURE,FotaState.OTA.FAILURE.CODE_INSTALL_VERIFY_PKI, "");
        }
    }
}
