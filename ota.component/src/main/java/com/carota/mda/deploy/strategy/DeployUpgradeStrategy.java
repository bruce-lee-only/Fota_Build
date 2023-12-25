/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.deploy.strategy;

import com.carota.mda.deploy.DeviceUpdater;
import com.carota.mda.deploy.db.DeploySdaDb;
import com.carota.mda.deploy.task.BaseDeployStratege;
import com.carota.mda.deploy.task.DeployTask;
import com.carota.mda.deploy.task.DeployTaskFactory;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.mda.telemetry.FotaState;
import com.momock.util.Logger;

public class DeployUpgradeStrategy extends BaseDeployStratege  {


    public DeployUpgradeStrategy(DeployTaskFactory factory, DeployTask task) {
        super(factory, task);
        DeploySdaDb.getmInstances().saveTaskInit(mTask.name);
    }

    @Override
    protected boolean install() {
        if (!DeploySdaDb.getmInstances().canUpgrade(mTask.name, mTask.group)) {
            Logger.debug("SDA %s Skip Upgrade ", mTask.name);
            return false;
        }
        ensureSafety(mTask.name);
        Logger.debug("SDA %s Start upgrade ",mTask.name);
        mDataFactory.getmResult().updateEcuUpgrading(mTask.name);
        DeploySdaDb.getmInstances().saveTaskStart(mTask.name);
        mDataFactory.saveLogStateEventV2(mTask.name, FotaState.OTA.STATE_UPGRADE, FotaState.OTA.STATE_UPGRADE, FotaState.OTA.UPGRADE.CODE_INSTALL_CONDITION);
        return doTask();
    }

    public boolean waitResult() {
        Logger.debug("SDA Wait %s upgrade End ",mTask.name);
        return doTask();
    }

    private boolean doTask() {
        DeviceUpdater updater = mDataFactory.getDeviceUpdater(false, mTask,this);
        Integer call = DeviceUpdater.RET_ERROR;
        if (null != updater) {
            call = updater.call();
            Logger.info("SDA %s is Upgreaded : %d ", mTask.name,call);
            // report hypothetical state event
            addFakeLogicReport(mTask.name, updater.getStep(), mTask.isSecurityEnable(), FotaAnalytics.OTA.TARGET_UPGRADE_DST, false);
        } else {
//            mDataFactory.saveLogStateEventV2(mTask.name, FotaState.OTA.STATE_UPGRADE, FotaState.OTA.STATE_UPDATE_FAILURE, FotaState.OTA.FAILURE.CODE_INSTALL_VERIFY_MD5);
            mDataFactory.saveLogStateEventV2(mTask.name, FotaState.OTA.STATE_UPGRADE, FotaState.OTA.STATE_UPDATE_FAILURE, FotaState.OTA.FAILURE.CODE_INSTALL_VERIFY_PKI);
        }
        boolean success = (call == DeviceUpdater.RET_SUCCESS);
        updateResult(success);
        return success;
    }

    @Override
    protected void updateResult(boolean success) {
        int status = success ? FotaAnalytics.OTA.STATE_SUCCESS : FotaAnalytics.OTA.STATE_FAIL;
        mDataFactory.getmResult().updateEcuUpgradEnd(mTask.name,success);
        DeploySdaDb.getmInstances().saveTaskUpgradeEnd(mTask.name,success,mTask.group);
        mDataFactory.saveLogStateEventV2(mTask.name, FotaState.OTA.STATE_UPGRADE, status, FotaState.OTA.UPGRADE.CODE_INSTALL_TRIGGER);
        setStatusName(status, mTask.name);
    }

    private void setStatusName(int status, String name) {
        switch (status) {
            case FotaAnalytics.OTA.STATE_SUCCESS:
                mDataFactory.setSuccessName(name);
                break;
            case FotaAnalytics.OTA.STATE_ROLLBACK_SUCCESS:
                mDataFactory.setErrorName(name);
                break;
            case FotaAnalytics.OTA.STATE_ROLLBACK_FAIL:
                mDataFactory.setFailerName(name);
                break;
        }
    }

}
