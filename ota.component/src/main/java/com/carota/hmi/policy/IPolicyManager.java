package com.carota.hmi.policy;

import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.task.callback.InitTaskCallback;
import com.carota.hmi.type.UpgradeType;

public interface IPolicyManager {

    Runnable getInitPolicy(InitTaskCallback callback);

    IPolicy getNewPolicy(UpgradeType type, ITaskCallback callback);
}
