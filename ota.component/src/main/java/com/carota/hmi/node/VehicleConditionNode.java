package com.carota.hmi.node;

import android.os.Handler;

import com.carota.CarotaVehicle;
import com.carota.core.VehicleCondition;
import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.status.HmiStatus;
import com.carota.hmi.status.IStatus;
import com.momock.util.Logger;

import java.util.ArrayList;

class VehicleConditionNode extends BaseNode {
    private VehicleCondition condition;

    VehicleConditionNode(HmiStatus hmiStatus, Handler handler, CallBackManager callback) {
        super(hmiStatus, handler, callback);
    }

    @Override
    public EventType getType() {
        return EventType.CONDITION;
    }

    @Override
    protected boolean execute() {
        return vehicleCondition();
    }

    public VehicleCondition getVehicleCondition() {
        return condition;
    }

    private boolean vehicleCondition() {
        ArrayList<IStatus.IConditionItem> result = new ArrayList<>();
        condition = CarotaVehicle.queryVehicleCondition();
        boolean isSuccess = true;
        if (condition != null) {
            for (VehicleCondition.Precondition c : VehicleCondition.parseFromSession(mStatus.getSession())) {
                boolean meet = condition.meet(c);
                result.add(new IStatus.IConditionItem(c.ID, meet));
                Logger.info("HMI Vehicle Condition %s is meet:%b", c.ID, meet);
                if (!condition.meet(c)) {
                    isSuccess = false;
                }
            }
        }
        mStatus.setCondition(result);
        return isSuccess;
    }
}
