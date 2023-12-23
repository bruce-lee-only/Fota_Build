/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.deploy.task;

import android.content.Context;
import android.text.TextUtils;

import com.carota.build.ParamDM;
import com.carota.build.ParamMDA;
import com.carota.build.ParamRoute;
import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.data.UpdateItem;
import com.carota.mda.deploy.DeviceUpdater;
import com.carota.mda.deploy.IDeploySafety;
import com.carota.mda.deploy.TriggeredRecord;
import com.carota.mda.deploy.bean.DeployResult;
import com.carota.mda.deploy.db.DeploySdaDb;
import com.carota.mda.download.DownloadCtrl;
import com.carota.mda.remote.IActionSDA;
import com.carota.mda.remote.info.MetaInfo;
import com.carota.mda.security.SecurityCenter;
import com.carota.mda.security.SecurityData;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.util.ConfigHelper;
import com.carota.util.SecurityUtil;
import com.momock.util.Logger;

import java.io.File;

import com.carota.mda.telemetry.FotaState;

public class DeployTaskFactory {
    private IActionSDA mActionSDA;
    private ParamRoute mParamRoute;
    private UpdateCampaign mSession;
    private FotaAnalytics mAnalytics;
    private DownloadCtrl mDownloadCtrl;
    private ParamMDA mParamMDA;
    private SecurityCenter mSecurity;
    private IDeploySafety mSafety;
    private TriggeredRecord mTriggeredRecord;
    private DeployResult mResult;
    private File mDownloadDir;

    //最终升级成功ecu名字
    private String successName;
    //最终升级回滚成功ecu名字
    private String errorName;

    public String getSuccessName() {
        return successName;
    }

    public synchronized void setSuccessName(String successName) {
        this.successName = successName;
    }

    public String getErrorName() {
        return errorName;
    }

    public synchronized void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public String getFailerName() {
        return failerName;
    }

    public synchronized void setFailerName(String failerName) {
        this.failerName = failerName;
    }

    //最终回滚失败的ecu名字
    private String failerName;

    private DeployTaskFactory() {
    }

    public synchronized void saveLogStateEventV2(String name, int status, int eState, int code) {
        mAnalytics.logUpgradeStateV2(mSession.getUSID(), name, status, eState, code, "");
    }

    private boolean isSecureEnable() {
        return !TextUtils.isEmpty(mSession.getTokenUrl());
    }

    private String getUsbDmHost() {
        return mSession.getRawData().optString("usb", "");
    }

    public DeployResult getmResult() {
        return mResult;
    }

    public synchronized DeviceUpdater getDeviceUpdater(boolean isRollback, DeployTask info, DeviceUpdater.IEventListener listener) {
        SecurityData tgtSec, srcSec;
        String tgtSignId = null, srcSignId = null;
        String dmHost = mDownloadCtrl.findDownloaderHost(info.name);
        String usbDmHost = getUsbDmHost();
        if (!TextUtils.isEmpty(usbDmHost)) {
            dmHost = usbDmHost;
        }
        if (info.isSecurityEnable() && mParamMDA.isSecureEnabled(info.name)) {
            Logger.info("DeviceUpdater @ SEC ON");
            tgtSec = mSecurity.load(info.targetFileId);
            if (null == tgtSec) {
                Logger.error("DeviceUpdater @ Missing SEC TGT");
                return null;
            }

            tgtSignId = SecurityUtil.findSignFile(tgtSec.md5, mDownloadDir, dmHost, MetaInfo.PROP_DST);
            if (!mSecurity.verifyPackage(dmHost, info.targetFileId, tgtSignId)) {
                Logger.error("DeviceUpdater @ Fail to Verify TGT");
                return null;
            }

            if (!TextUtils.isEmpty(info.sourceFileId)) {
                srcSec = mSecurity.load(info.sourceFileId);
                if (null == srcSec) {
                    Logger.error("DeviceUpdater # Missing SEC SRC");
                    return null;
                }
                srcSignId = SecurityUtil.findSignFile(tgtSec.md5, mDownloadDir, dmHost, MetaInfo.PROP_SRC);
                if (!mSecurity.verifyPackage(dmHost, info.sourceFileId, srcSignId)) {
                    Logger.error("DeviceUpdater @ Fail to Verify SRC");
                    return null;
                }
            }
        } else {
            Logger.info("Updater @ SEC OFF");
        }

        String ecuHost = ParamRoute.getEcuHost(mParamRoute.getRoute(info.name));
        if (TextUtils.isEmpty(ecuHost)) ecuHost = mParamRoute.getSubHost();
        DeviceUpdater deviceUpdater = new DeviceUpdater(mActionSDA, mParamMDA.getTimeout(), false, listener, mSession.getBomInfo(info.name));
        String targetId;

        if (isRollback) {
            //回滚
            targetId = info.sourceFileId;
            deviceUpdater.setDevice(ecuHost, info.name, dmHost, 0);
            deviceUpdater.setTarget(info.sourceFileId, info.sourceVer, srcSignId);
            deviceUpdater.setSource(null, null, null);
        } else {
            //升级
            targetId = info.targetFileId;
            deviceUpdater.setDevice(ecuHost, info.name, dmHost, 0);
            deviceUpdater.setTarget(info.targetFileId, info.targetVer, tgtSignId);
            deviceUpdater.setSource(info.sourceFileId, info.sourceVer, srcSignId);
        }
        deviceUpdater.setResume(mTriggeredRecord.isTriggered(info.name, targetId));
        mTriggeredRecord.setTriggered(info.name, targetId);
        return deviceUpdater;
    }

    public void setSession(UpdateCampaign session) {
        this.mSession = session;
        mResult = new DeployResult(session.getUSID());
    }

    public DeployTask create(UpdateItem task) {
        return new DeployTask(task, isSecureEnable());
    }

    public String getUsid() {
        return mSession.getUSID();
    }

    public synchronized boolean ensureSafety(String name) {
        mResult.setBlock(true);
        if (null != mSafety && !mSafety.ensureSafety(0, name)) {
            return false;
        }
        mResult.setBlock(false);
        return true;
    }

    public synchronized void onInstallEnd() {
        if (!TextUtils.isEmpty(failerName)) {
            saveLogStateEventV2(failerName, FotaState.OTA.STATE_ROLLBACK_FAILURE, FotaState.OTA.STATE_ROLLBACK_FAILURE, 0);
        } else if (!TextUtils.isEmpty(errorName)) {
            saveLogStateEventV2(errorName, FotaState.OTA.STATE_ROLLBACK_SUCCESS, FotaState.OTA.STATE_ROLLBACK_SUCCESS, 0);
        } else if (!TextUtils.isEmpty(successName)) {
            saveLogStateEventV2(successName, FotaState.OTA.STATE_UPGRADE_SUCCESS, FotaState.OTA.STATE_UPGRADE_SUCCESS, 0);
        }
        failerName = null;
        errorName = null;
        successName = null;
        mAnalytics.uploadLog(mResult.getmStatus() == DeployResult.FAILURE || mResult.getmStatus() == DeployResult.ERROR);
    }

    public static class Builder {
        private final DeployTaskFactory mDeployTaskFactory;

        public Builder(Context context) {
            DeploySdaDb.getmInstances().init(context.getApplicationContext());
            mDeployTaskFactory = new DeployTaskFactory();
            mDeployTaskFactory.mTriggeredRecord = TriggeredRecord.get(context);
            mDeployTaskFactory.mDownloadDir = ConfigHelper.get(context).get(ParamDM.class).getDownloadDir(context);
        }

        public Builder setActionSDA(IActionSDA actionSDA) {
            mDeployTaskFactory.mActionSDA = actionSDA;
            return this;
        }

        public Builder setParamRoute(ParamRoute paramRoute) {
            mDeployTaskFactory.mParamRoute = paramRoute;
            return this;
        }

        public Builder setAnalytics(FotaAnalytics analytics) {
            mDeployTaskFactory.mAnalytics = analytics;
            return this;
        }

        public Builder setDownloader(DownloadCtrl downloadCtrl) {
            mDeployTaskFactory.mDownloadCtrl = downloadCtrl;
            return this;
        }

        public Builder setSecure(SecurityCenter center) {
            mDeployTaskFactory.mSecurity = center;
            return this;
        }

        public Builder setSafety(IDeploySafety safety) {
            mDeployTaskFactory.mSafety = safety;
            return this;
        }

        public DeployTaskFactory build() {
            if (mDeployTaskFactory.mDownloadCtrl == null) {
                throw new RuntimeException("DmHoust is null");
            }

            if (mDeployTaskFactory.mActionSDA == null) {
                throw new RuntimeException("ActionSDA is null");
            }
            if (mDeployTaskFactory.mParamRoute == null) {
                throw new RuntimeException("ParamRoute is null");
            }
            if (mDeployTaskFactory.mAnalytics == null) {
                throw new RuntimeException("Analytics is null");
            }
            return mDeployTaskFactory;
        }

        public Builder setParamMDA(ParamMDA paramMDA) {
            mDeployTaskFactory.mParamMDA = paramMDA;
            return this;
        }
    }
}
