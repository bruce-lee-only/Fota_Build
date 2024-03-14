/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.deploy.ctrl;

import com.carota.mda.deploy.db.DeploySdaDb;
import com.carota.mda.deploy.strategy.DeployAsncStrategy;
import com.carota.mda.deploy.strategy.DeployRollbackStrategy;
import com.carota.mda.deploy.strategy.DeployUpgradeStrategy;
import com.carota.mda.deploy.strategy.IDeployStratege;
import com.carota.mda.deploy.task.DeployTask;
import com.carota.mda.deploy.task.DeployTaskFactory;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 行控制器
 */
public class DeployLineCtrl {
    private final DeployTaskFactory mFactory;
    private final boolean isRollback;
    private final List<IDeployStratege> mStrategeList;

    DeployLineCtrl(DeployTaskFactory factory) {

        this.mFactory = factory;
        isRollback = DeploySdaDb.getmInstances().isRollbacking();
        mStrategeList = new ArrayList<>();
    }

    /**
     * install ecu
     */
    public void install() {
        for (int i = 0; i < mStrategeList.size(); i++) {
            try {
                mStrategeList.get(i).run();
                Thread.sleep(3000);
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    public void stop() {
        //if stop，Intercept install fun
    }

    void addTask(List<DeployTask> task) {
        mStrategeList.add(getIDeployStratege(task));
    }

    private IDeployStratege getIDeployStratege(List<DeployTask> tasks) {
        return new DeployAsncStrategy(tasks,isRollback,mFactory);
    }

}
