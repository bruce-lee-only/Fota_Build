package com.carota.hmi.policy;

import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.type.UpgradeType;

public class SchedulePolicy extends BasePolicy {
    public SchedulePolicy(ITaskCallback callback) {
        super(callback);
    }

    @Override
    public UpgradeType getUpgradeType() {
        return UpgradeType.SCHEDULE;
    }
}
