package com.carota.hmi.task.callback;

import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;

public interface ITaskCallback {

    void taskStart(HmiTaskType type);

    void taskEnd(HmiTaskType type, IHmiCallback.IHmiResult result);

    void taskRunEnd(boolean keepDownload);

    void updateInstallType();

    UpgradeType getUpgradeType();
}
