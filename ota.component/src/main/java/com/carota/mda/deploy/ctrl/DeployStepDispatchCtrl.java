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

import com.carota.mda.deploy.task.DeployTaskFactory;
import com.carota.mda.deploy.task.DeployTask;
import com.momock.util.Logger;

/**
 * step ctrl
 */
public class DeployStepDispatchCtrl {
    private final DeployStepCtrl mCtrl;
    private final DeployTaskFactory mFactory;
    private int mStep;
    private DeployStepDispatchCtrl mNextCtrl;

    DeployStepDispatchCtrl(DeployTask task, DeployTaskFactory factory) {
        mFactory = factory;
        mCtrl = new DeployStepCtrl(factory);
        mStep = task.step;
        mCtrl.addTask(task);
    }

    void addTask(DeployTask task) {
        if (mStep == task.step) {
            mCtrl.addTask(task);
        } else {
            if (mNextCtrl == null) {
                mNextCtrl = new DeployStepDispatchCtrl(task, mFactory);
            } else {
                mNextCtrl.addTask(task);
            }
        }
    }

    public void install() {
        Logger.info("SDA Start Step:%d",mStep);
        mCtrl.install();
        if (mNextCtrl != null) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Logger.error(e);
            }
            mNextCtrl.install();
        }
    }

    public void stop() {
        mCtrl.stop();
        if (mNextCtrl != null) {
            mNextCtrl.stop();
        }
    }
}
