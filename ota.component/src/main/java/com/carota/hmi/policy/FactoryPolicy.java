package com.carota.hmi.policy;

import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.type.UpgradeType;

public class FactoryPolicy extends BasePolicy {

    public FactoryPolicy(ITaskCallback callback) {
        super(callback);
    }

    @Override
    public boolean isFactory() {
        return true;
    }

    @Override
    public UpgradeType getUpgradeType() {
        return UpgradeType.FACTORY;
    }
}
