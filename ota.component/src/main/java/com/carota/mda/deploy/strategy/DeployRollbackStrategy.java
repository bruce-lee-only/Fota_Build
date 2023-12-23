package com.carota.mda.deploy.strategy;

import com.carota.mda.deploy.DeviceUpdater;
import com.carota.mda.deploy.db.DeploySdaDb;
import com.carota.mda.deploy.task.BaseDeployStratege;
import com.carota.mda.deploy.task.DeployTask;
import com.carota.mda.deploy.task.DeployTaskFactory;
import com.carota.mda.telemetry.FotaAnalytics;
import com.carota.mda.telemetry.FotaState;
import com.momock.util.Logger;

public class DeployRollbackStrategy extends BaseDeployStratege {


    public DeployRollbackStrategy(DeployTaskFactory factory, DeployTask task) {
        super(factory, task);
        DeploySdaDb.getmInstances().saveTaskRollbackInit(mTask.name);
    }

    @Override
    protected boolean install() {
        if (!DeploySdaDb.getmInstances().canRollback(mTask.group, mTask.name)) {
            Logger.debug("SDA %s Skip rollback ", mTask.name);
            return false;
        }
        Logger.debug("SDA %s Start rollback ",mTask.name);
        mDataFactory.getmResult().updateEcuRollbacking(mTask.name);
        DeploySdaDb.getmInstances().saveTaskRollbackStart(mTask.name);
        return doTask();
    }

    @Override
    protected boolean waitResult() {
        Logger.debug("SDA Wait %s rollback End ",mTask.name);
        return doTask();
    }

    private boolean doTask() {
        DeviceUpdater updater = mDataFactory.getDeviceUpdater(true, mTask,this);
        Integer call = DeviceUpdater.RET_ERROR;
        if (null != updater) {
            call = updater.call();
            Logger.info("SDA %s is Rollbacked :%d！", mTask.name,call);
            addFakeLogicReport(mTask.name, updater.getStep(), mTask.isSecurityEnable(), FotaAnalytics.OTA.TARGET_UPGRADE_SRC, true);
        } else {
//            mDataFactory.saveLogStateEventV2(mTask.name, FotaState.OTA.STATE_ROLLBACK, FotaState.OTA.STATE_ROLLBACK_FAILURE, FotaState.OTA.FAILURE.CODE_INSTALL_VERIFY_MD5);
            mDataFactory.saveLogStateEventV2(mTask.name, FotaState.OTA.STATE_ROLLBACK, FotaState.OTA.STATE_ROLLBACK_FAILURE, FotaState.OTA.FAILURE.CODE_INSTALL_VERIFY_PKI);
        }
        boolean success = call == DeviceUpdater.RET_SUCCESS;
        updateResult(success);
        setStatusName(success ? FotaAnalytics.OTA.STATE_ROLLBACK_SUCCESS : FotaAnalytics.OTA.STATE_ROLLBACK_FAIL, mTask.name);
        return success;
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

    @Override
    protected void updateResult(boolean success) {
        int status = success ? FotaAnalytics.OTA.STATE_ROLLBACK_SUCCESS : FotaAnalytics.OTA.STATE_ROLLBACK_FAIL;
        mDataFactory.saveLogStateEventV2(mTask.name, FotaState.OTA.STATE_UPGRADE, status, 0);
        mDataFactory.getmResult().updateEcuRollbackEnd(mTask.name,success);
        DeploySdaDb.getmInstances().saveTaskRollbackEnd(mTask.name,success,mTask.group);

    }

}
