package com.carota.mda.deploy.task;

import com.carota.mda.deploy.DeviceUpdater;
import com.carota.mda.deploy.db.DeploySdaDb;
import com.carota.mda.remote.info.SlaveInstallResult;
import com.carota.mda.telemetry.FotaState;
import com.momock.util.Logger;

public abstract class BaseDeployStratege implements DeviceUpdater.IEventListener {
    protected final DeployTaskFactory mDataFactory;
    protected final DeployTask mTask;

    public BaseDeployStratege(DeployTaskFactory factory, DeployTask task) {
        this.mDataFactory = factory;
        this.mTask = task;
        Logger.info("SDA Add Ecu:%s,Step:%d,Line:%d,Group:%d", task.name, task.step, task.line, task.group);
    }

    public final boolean run() {
        try {
            if (DeploySdaDb.getmInstances().isRuning(mTask.name)) {
                ensureSafety(mTask.name);
                return waitResult();
            } else {
                return install();
            }
        } catch (Exception e) {
            Logger.error(e);
            updateResult(false);
        }
        return false;
    }

    protected void ensureSafety(String name) {
        if (!mDataFactory.ensureSafety(name)) {
            throw new SecurityException(String.format("SDA %s Ensure Safety Error", name));
        }
    }

    protected abstract boolean install();

    protected abstract boolean waitResult();

    protected abstract void updateResult(boolean success);

    protected final void addFakeLogicReport(String name, int errorCode, boolean secure, int target, boolean rollback) {
        int state = rollback ? FotaState.OTA.STATE_ROLLBACK : FotaState.OTA.STATE_UPGRADE;
        int errCode = FotaState.OTA.UPGRADE.CODE_INSTALL_CONDITION;
        if (SlaveInstallResult.STEP_NONE == errorCode || SlaveInstallResult.STEP_DEPLOY == errorCode) {
            errCode = rollback ? FotaState.OTA.ROLLBACK.CODE_INSTALL_VERIFY_MD5 : FotaState.OTA.UPGRADE.CODE_INSTALL_VERIFY_MD5;
            mDataFactory.saveLogStateEventV2(name, state, state, errCode);
            if (secure) {
                errCode = rollback ? FotaState.OTA.ROLLBACK.CODE_INSTALL_VERIFY_PKI : FotaState.OTA.UPGRADE.CODE_INSTALL_VERIFY_PKI;
                mDataFactory.saveLogStateEventV2(name, state, state, errCode);
            }
        } else if (SlaveInstallResult.STEP_TRANSPORT == errorCode) {
            errCode = rollback ? FotaState.OTA.ROLLBACK.CODE_INSTALL_TRANSFER : FotaState.OTA.FAILURE.CODE_INSTALL_TRANSFER;
            mDataFactory.saveLogStateEventV2(name, state, state, errCode);
        } else if (SlaveInstallResult.STEP_VERIFY == errorCode) {
            mDataFactory.saveLogStateEventV2(name, state, state, rollback ? FotaState.OTA.ROLLBACK.CODE_INSTALL_VERIFY_MD5 : FotaState.OTA.ROLLBACK.CODE_INSTALL_VERIFY_MD5);
            mDataFactory.saveLogStateEventV2(name, state, state, rollback ? FotaState.OTA.ROLLBACK.CODE_INSTALL_VERIFY_PKI : FotaState.OTA.ROLLBACK.CODE_INSTALL_VERIFY_PKI);
        }
    }

    @Override
    public void onProcess(int pro) {
        mDataFactory.getmResult().updateEcuPro(mTask.name, pro);
        DeploySdaDb.getmInstances().saveTaskPro(mTask.name, pro);
    }
}
