package com.carota.hmi.callback;

import com.carota.core.VehicleCondition;
import com.carota.hmi.action.ConditionAction;

import java.util.List;

public abstract class ICondition implements ICall {
    public abstract void onStop(boolean success, ConditionAction action, List<IConditionItem> result);

    public static class IConditionItem {
        public VehicleCondition.Item item;
        public boolean success;

        public VehicleCondition.Item getItem() {
            return item;
        }

        public boolean isSuccess() {
            return success;
        }

        public IConditionItem(VehicleCondition.Item item, boolean success) {
            this.item = item;
            this.success = success;
        }
    }
}
