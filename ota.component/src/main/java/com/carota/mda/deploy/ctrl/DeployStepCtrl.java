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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 步控制器
 */
class DeployStepCtrl {
    private final DeployTaskFactory mFactory;
    private Map<Integer, DeployLineCtrl> mData;
    private ExecutorService mExecutorService;


    DeployStepCtrl(DeployTaskFactory factory) {
        mFactory = factory;
        mData = new HashMap<>();
    }

    void addTask(DeployTask task) {
        int line = task.line;
        DeployLineCtrl deploySyncCtrl = mData.get(line);
        if (deploySyncCtrl == null) {
            deploySyncCtrl = new DeployLineCtrl(mFactory);
            mData.put(line, deploySyncCtrl);
        }
        deploySyncCtrl.addTask(task);
    }

    public void install() {
        if (mExecutorService == null) {
            mExecutorService = Executors.newFixedThreadPool(5);
        }
        AtomicInteger integer = new AtomicInteger(0);
        for (final Map.Entry<Integer, DeployLineCtrl> next : mData.entrySet()) {
            mExecutorService.execute(() -> {
                try {
                    next.getValue().install();
                } catch (Exception e) {
                    Logger.error(e);
                }
                integer.incrementAndGet();
            });
        }
        while (mData.size()!=integer.get()) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
    }

    public void stop() {
        for (DeployLineCtrl syncCtrl : mData.values()) {
            syncCtrl.stop();
        }
    }

}
