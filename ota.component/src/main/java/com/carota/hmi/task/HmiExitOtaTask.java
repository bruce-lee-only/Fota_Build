package com.carota.hmi.task;

import com.carota.CarotaVehicle;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.type.HmiTaskType;
import com.momock.util.Logger;

public final class HmiExitOtaTask extends BaseTask {
    public HmiExitOtaTask() {
        super();
    }

    @Override
    IHmiCallback.IHmiResult runNode() {
        while (true) {
            try {
                if (CarotaVehicle.setUpgradeRuntimeEnable(false, false)) {
                    break;
                }
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
        return new IHmiCallback.IHmiResult(true);
    }

    @Override
    public HmiTaskType getType() {
        return HmiTaskType.exit_ota;
    }
}
