package com.carota.hmi.policy;

import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.type.UpgradeType;

/**
 * The Defult Policy
 */
public class DefultPolicy extends BasePolicy {

    public DefultPolicy(ITaskCallback callback) {
        super(callback);
    }


    @Override
    public UpgradeType getUpgradeType() {
        return UpgradeType.DEFULT;
    }
}
