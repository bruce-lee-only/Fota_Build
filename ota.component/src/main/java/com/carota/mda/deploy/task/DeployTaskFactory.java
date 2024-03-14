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
import com.carota.mda.deploy.DeviceUpdater;
import com.carota.mda.deploy.IDeploySafety;
import com.carota.mda.deploy.TriggeredRecord;
import com.carota.mda.deploy.bean.DeployResult;
import com.carota.mda.deploy.db.DeploySdaDb;
import com.carota.mda.download.DownloadCtrl;
import com.carota.mda.remote.ActionDM;
import com.carota.mda.remote.IActionSDA;
import com.carota.mda.remote.info.BomInfo;
import com.carota.mda.remote.info.MetaInfo;
import com.carota.mda.security.SecurityCenter;
import com.carota.mda.security.SecurityData;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.util.ConfigHelper;
import com.carota.util.SecurityUtil;
import com.momock.util.EncryptHelper;
import com.momock.util.Logger;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

    //最终回滚失败的ecu名字
    private String failerName;

    private final Map<String, DeviceUpdater.IInstallListener> mLisinerMap;

    public synchronized void setSuccessName(String successName) {
        this.successName = successName;
    }

    public synchronized void setErrorName(String errorName) {
        this.errorName = errorName;
    }

    public synchronized void setFailerName(String failerName) {
        this.failerName = failerName;
    }

    private DeployTaskFactory() {
        mLisinerMap = new HashMap<>();
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
        String dmHost = getDMHost(info.name);
        String ecuHost = getEcuHost(info.name, info.havaBom());
        if (!taskVerify(info, dmHost)) return null;
        if (!calMd5(isRollback, info, dmHost)) return null;
        Logger.info("SDA `%1s` Bom is:%2s", info.name, info.bomInfo);
        DeviceUpdater deviceUpdater = new DeviceUpdater(mActionSDA, mParamMDA.getTimeout(), false, listener, info.bomInfo);
        String targetId;
        if (isRollback) {
            //回滚
            targetId = info.sourceFileId;
            deviceUpdater.setDevice(ecuHost, info.name, dmHost, 0);
            deviceUpdater.setTarget(info.sourceFileId, info.sourceVer, info.srcSignId);
            deviceUpdater.setSource(null, null, null);
        } else {
            //升级
            targetId = info.targetFileId;
            deviceUpdater.setDevice(ecuHost, info.name, dmHost, 0);
            deviceUpdater.setTarget(info.targetFileId, info.targetVer, info.tgtSignId);
            deviceUpdater.setSource(info.sourceFileId, info.sourceVer, info.srcSignId);
        }
        if (info.havaBom()) {
            info.bomInfo.setMd5(targetId);
            info.bomInfo.setUrl(getDownloadUrl(dmHost, targetId));
        }
        deviceUpdater.setInstallListener(mLisinerMap.get(info.name));
        deviceUpdater.setResume(mTriggeredRecord.isTriggered(info.name, targetId));
        mTriggeredRecord.setTriggered(info.name, targetId);
        return deviceUpdater;
    }

    private String getDownloadUrl(String host, String name) {
        return String.format("http://%1s/file?id=%2s", host, name);
    }

    private boolean taskVerify(DeployTask info, String dmHost) {
        info.tgtSignId = null;
        info.srcSignId = null;
        String dmName = mParamMDA.findDownloadManagerName(info.name);

        if (info.isSecurityEnable() && mParamMDA.isSecureEnabled(info.name)) {
            SecurityData tgtSec, srcSec;
            Logger.info("SDA DeviceUpdater @ SEC ON");
            tgtSec = mSecurity.load(info.targetFileId);
            if (null == tgtSec) {
                Logger.error("SDA DeviceUpdater @ Missing SEC TGT");
                return false;
            }

            info.tgtSignId = SecurityUtil.findSignFile(tgtSec.md5, mDownloadDir, dmHost, MetaInfo.PROP_DST);
            if (!mSecurity.verifyPackage(dmHost, info.targetFileId, info.tgtSignId)) {
                Logger.error("SDA DeviceUpdater @ Fail to Verify TGT");
                info.tgtSignId = null;
                return false;
            }

            if (!TextUtils.isEmpty(info.sourceFileId)) {
                srcSec = mSecurity.load(info.sourceFileId);
                if (null == srcSec) {
                    Logger.error("SDA DeviceUpdater # Missing SEC SRC");
                    return false;
                }
                info.srcSignId = SecurityUtil.findSignFile(tgtSec.md5, mDownloadDir, dmHost, MetaInfo.PROP_SRC);
                if (!mSecurity.verifyPackage(dmHost, info.sourceFileId, info.srcSignId)) {
                    Logger.error("SDA DeviceUpdater @ Fail to Verify SRC");
                    info.srcSignId = null;
                    return false;
                }
            }
        } else {
            Logger.info("SDA Updater @ SEC OFF");
        }
        return true;
    }

    private String getEcuHost(String name, boolean isBom) {
        String host = ParamRoute.getEcuHost(mParamRoute.getRoute(name));
        if (TextUtils.isEmpty(host)) host = mParamRoute.getSubHost();
        return host;
    }

    private String getDMHost(String ecu) {
        String dmHost = mDownloadCtrl.findDownloaderHost(ecu);
        String usbDmHost = getUsbDmHost();
        if (!TextUtils.isEmpty(usbDmHost)) {
            dmHost = usbDmHost;
        }
        return dmHost;
    }

    public void setSession(UpdateCampaign session) {
        this.mSession = session;
        mResult = new DeployResult(session.getUSID());
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

    public void setInstallLisiner(String ecu, DeviceUpdater.IInstallListener lisiner) {
        mLisinerMap.put(ecu, lisiner);
    }

    public void clearInstallLisiner(DeviceUpdater.IInstallListener lisiner) {
        Iterator<DeviceUpdater.IInstallListener> iterator = mLisinerMap.values().iterator();
        while (iterator.hasNext()) {
            if (lisiner == iterator.next()) {
                iterator.remove();
            }
        }
    }

    public boolean prepareInstall(List<BomInfo> bomInfos) {
        return mActionSDA.prepareInstall(getEcuHost(bomInfos.get(0).ID, true), bomInfos);
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

    private boolean calMd5(boolean isRollback, DeployTask info, String dmHost) {
        boolean needCalMd5 = isRollback ? TextUtils.isEmpty(info.srcSignId) : TextUtils.isEmpty(info.tgtSignId);
        if (!needCalMd5) {
            Logger.info("SDA Skip calMd5,because pki is Ok");
            return true;
        }
        String dmName = mParamMDA.findDownloadManagerName(info.name);
        String md5 = isRollback ? info.sourceFileId : info.targetFileId;
        Logger.info("SDA calMd5 dmName = %s, dmHost = %s, md5 = %s", dmName, dmHost, md5);
        boolean result = false;
        try {
            InputStream metaStream = ActionDM.openInputStream(dmHost, md5);
            String calMd5 = EncryptHelper.calcFileMd5(metaStream);
            Logger.info("SDA calMd5 metaStream = %s", calMd5);
            result = md5.equals(calMd5);
            metaStream.close();
        } catch (Exception e) {
            Logger.error(e);
        }
        Logger.info("SDA calMd5 result = %s", result);
        return result;
    }
}
