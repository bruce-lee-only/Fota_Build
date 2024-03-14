package com.carota.hmi.policy;

import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.type.UpgradeType;

public class PushUpgradePolicy extends BasePolicy {
    public PushUpgradePolicy(ITaskCallback callback) {
        super(callback);
    }


    @Override
    public UpgradeType getUpgradeType() {
        return UpgradeType.PUSH_UPGRADE;
    }
}
