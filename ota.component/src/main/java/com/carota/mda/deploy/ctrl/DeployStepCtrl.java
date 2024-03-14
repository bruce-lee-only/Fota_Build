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

import com.carota.mda.deploy.task.DeployTask;
import com.carota.mda.deploy.task.DeployTaskFactory;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private final Map<Integer, List<DeployTask>> mMap;
    private ExecutorService mExecutorService;


    DeployStepCtrl(DeployTaskFactory factory) {
        mFactory = factory;
        mMap = new HashMap<>();
    }

    /**
     * @param task 将相同line的task放入到一个list当中存放
     */
    void addTask(DeployTask task) {
        int line = task.line;
        List<DeployTask> list = mMap.get(line);
        if (list == null) {
            list = new ArrayList<>();
            mMap.put(line, list);
        }
        list.add(task);
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
        while (mData.size() != integer.get()) {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        mData.clear();
    }

    public void stop() {
        if (mData != null) {
            for (DeployLineCtrl syncCtrl : mData.values()) {
                syncCtrl.stop();
            }
        }
    }

    public void addTaskEnd() {
        mData = new HashMap<>();
        setNewStrategy();
        //常规逻辑按照原方案执行刷写
        for (Map.Entry<Integer, List<DeployTask>> entry : mMap.entrySet()) {
            Integer line = entry.getKey();
            for (DeployTask task : entry.getValue()) {
                DeployLineCtrl ctrl = mData.get(line);
                if (ctrl == null) {
                    ctrl = new DeployLineCtrl(mFactory);
                    mData.put(line, ctrl);
                }
                ctrl.addTask(Collections.singletonList(task));
            }
        }
        mMap.clear();
    }

    private void setNewStrategy() {
        List<DeployTask> data = new ArrayList<>();
        for (List<DeployTask> list : mMap.values()) {
            if (list.size() > 1) {
                data.clear();
                break;
            } else if (list.get(0)!=null&&list.get(0).havaBom()) {
                data.addAll(list);
            }
        }
        if (data.size() > 1) {
            // 每个line只有一个ecu时,将所有ecu合并到多ecu刷写容器同时执行刷写
            DeployLineCtrl deployLineCtrl = new DeployLineCtrl(mFactory);
            deployLineCtrl.addTask(data);
            mData.put(data.get(0).line, deployLineCtrl);
            for (DeployTask task : data) {
                mMap.remove(task.line);
            }
        }
        data.clear();
    }
}
