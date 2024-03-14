package com.carota.hmi.task;

import com.carota.CarotaClient;
import com.carota.CarotaVehicle;
import com.carota.core.VehicleCondition;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.callback.IHmiPolicyManager;
import com.carota.hmi.type.HmiTaskType;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;

public final class HmiConditionTask extends BaseTask {
    public HmiConditionTask() {
        super();
    }

    @Override
    IHmiCallback.IHmiResult runNode() {
        List<IHmiPolicyManager.IConditionItem> result = new ArrayList<>();
        boolean isSuccess = false;
        try {
            VehicleCondition condition = CarotaVehicle.queryVehicleCondition();
            if (condition != null) {
                isSuccess = true;
                for (VehicleCondition.Precondition c
                        : VehicleCondition.parseFromSession(CarotaClient.getClientSession())) {
                    boolean meet = condition.meet(c);
                    result.add(new IHmiPolicyManager.IConditionItem(c.ID, meet));
                    Logger.info("HMI-Task Vehicle Condition %s is meet:%b", c.ID, meet);
                    if (!condition.meet(c)) {
                        isSuccess = false;
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return new IHmiCallback.IHmiResult(isSuccess, result);
    }

    @Override
    public HmiTaskType getType() {
        return HmiTaskType.condition;
    }
}
