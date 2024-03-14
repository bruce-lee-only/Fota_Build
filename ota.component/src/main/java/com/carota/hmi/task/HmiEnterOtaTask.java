package com.carota.hmi.task;

import com.carota.CarotaVehicle;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.type.HmiTaskType;
import com.momock.util.Logger;

public final class HmiEnterOtaTask extends BaseTask {
    public HmiEnterOtaTask() {
        super();
    }

    @Override
    IHmiCallback.IHmiResult runNode() {
        boolean isSuccess = false;
        try {
            CarotaVehicle.setUpgradeRuntimeEnable(true, false);
        } catch (Exception e) {
            Logger.error(e);
        }
        return new IHmiCallback.IHmiResult(isSuccess);
    }

    @Override
    public HmiTaskType getType() {
        return HmiTaskType.enter_ota;
    }
}
