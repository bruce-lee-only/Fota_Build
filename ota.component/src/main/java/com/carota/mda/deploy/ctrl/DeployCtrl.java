package com.carota.mda.deploy.ctrl;

import static com.carota.mda.deploy.bean.DeployResult.ERROR;
import static com.carota.mda.deploy.bean.DeployResult.UPGRADE;

import android.text.TextUtils;

import com.carota.mda.data.UpdateCampaign;
import com.carota.mda.deploy.bean.DeployResult;
import com.carota.mda.deploy.db.DeploySdaDb;
import com.carota.mda.deploy.task.DeployTask;
import com.carota.mda.deploy.task.DeployTaskFactory;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

import java.util.List;

public class DeployCtrl {
    private final SerialExecutor mExecutor;
    private final DeployTaskFactory mFactory;
    private DeployStepDispatchCtrl mStepCtrl;
    private boolean isRun;

    public DeployCtrl(DeployTaskFactory factory) {
        mExecutor = new SerialExecutor();
        this.mFactory = factory;
    }

    /**
     * start install
     */
    public void start(UpdateCampaign session) {
        Logger.error("SDA Ctrl Start");
        mFactory.setSession(session);

        if (!session.check()) {
            Logger.info("SDA Ctrl not find need upgrade Ecu");
            //直接报回滚失败
            getResult().setTatolStatus(ERROR);
            return;
        }
        if (!getDbIsRun()) {
            DeploySdaDb.getmInstances().clearAllTab(session.getUSID());
        }
        isRun = true;

        if (!DeploySdaDb.getmInstances().isRollbacking()) {
            upgrade(session);
        } else {
            rollback(session);
        }
    }

    /**
     * 回滚
     * @param session
     */
    private void rollback(UpdateCampaign session) {
        List<Integer> group = DeploySdaDb.getmInstances().setStatusRollbackInit(getResult());
        if (group.isEmpty()) {
            Logger.error("SDA Ctrl not find need Rollback group,upgrade Success");
            mFactory.onInstallEnd();
            isRun = false;
            return;
        }
        Logger.error("SDA Ctrl Upgrade End ，But find Rollback Group：%s",group.toString());
        boolean b = !TextUtils.isEmpty(session.getSecurityUrl());
        for (int i = session.getItemCount()-1; i >=0; i--) {
            DeployTask task = new DeployTask(session.getItem(i), b);
            if (!group.contains(task.group)) {
                Logger.error("SDA Ctrl %s is continue",task.name);
                continue;
            }
            if (mStepCtrl == null) {
                mStepCtrl = new DeployStepDispatchCtrl(task, mFactory);
            } else {
                mStepCtrl.addTask(task);
            }
        }
        mExecutor.execute(() -> {
            Logger.error("SDA Ctrl start Rollback");
            DeploySdaDb.getmInstances().setStatusRollbacking();
            DeploySdaDb.getmInstances().createResult(getResult());
            if (mStepCtrl != null) mStepCtrl.install();
            Logger.error("SDA Ctrl Rollback End");
            int status = DeploySdaDb.getmInstances().setStatusRollbackEnd();
            getResult().setTatolStatus(status);
            mFactory.onInstallEnd();
            isRun = false;
            mStepCtrl = null;
        });
    }

    /**
     * 升级
     * @param session
     */
    private void upgrade(UpdateCampaign session) {
        getResult().setTatolStatus(UPGRADE);
        DeploySdaDb.getmInstances().setStatusUpgradeInit();
        boolean b = !TextUtils.isEmpty(session.getSecurityUrl());
        for (int i = 0; i < session.getItemCount(); i++) {
            DeployTask task = new DeployTask(session.getItem(i), b);
            if (mStepCtrl == null) {
                mStepCtrl = new DeployStepDispatchCtrl(task, mFactory);
            } else {
                mStepCtrl.addTask(task);
            }
        }
        mExecutor.execute(() -> {
            Logger.error("SDA Ctrl start Upgrade");
            DeploySdaDb.getmInstances().setStatusUpgrading();
            DeploySdaDb.getmInstances().createResult(getResult());
            if (mStepCtrl != null) mStepCtrl.install();
            mStepCtrl = null;
            rollback(session);
        });
    }

    public boolean getDbIsRun() {
        return DeploySdaDb.getmInstances().isRuning();

    }

    public boolean isRun() {
        return isRun;
    }

    public DeployResult getResult() {
        return mFactory.getmResult();
    }
}
