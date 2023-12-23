package com.carota.hmi;

import com.carota.core.VehicleCondition;
import com.carota.hmi.action.BaseAction;
import com.carota.hmi.callback.ICondition;

import java.util.ArrayList;
import java.util.List;

public interface ITaskVerifyResult {
    void result(VehicleCondition vehicleCondition, List<ICondition.IConditionItem> verifyResult);

    ArrayList<VehicleCondition.Precondition> addExtraPreCondition();
}
