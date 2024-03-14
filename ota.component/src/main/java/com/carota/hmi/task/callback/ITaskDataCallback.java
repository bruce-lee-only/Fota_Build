package com.carota.hmi.task.callback;

public interface ITaskDataCallback {
    boolean isFactory();

    void saveInstallType();

    boolean suspend();


}
