package com.carota.hmi.task.callback;

import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public interface ITask {
    boolean havaFailTask();

    void setSuccessIfNotSuccess(HmiTaskType toTaskType);

    boolean containsTask(HmiTaskType type);

    boolean run(ITaskCallback hmiCallback, UpgradeType upgradeType) throws Exception;

    void resetAllTask();

    void setEndTaskData(AtomicBoolean download, AtomicInteger needExitOta, AtomicBoolean taskAlive);
}
