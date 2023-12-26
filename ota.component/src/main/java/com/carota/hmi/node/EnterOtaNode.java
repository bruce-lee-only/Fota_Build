package com.carota.hmi.node;

import android.os.Handler;

import com.carota.CarotaVehicle;
import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.status.HmiStatus;

/**
 * enter ota
 */
class EnterOtaNode extends BaseNode {
    EnterOtaNode(HmiStatus hmiStatus, Handler handler, CallBackManager callback) {
        super(hmiStatus, handler, callback);
    }

    @Override
    public EventType getType() {
        return EventType.ENTER_OTA;
    }

    @Override
    protected boolean execute() {
        if (!CarotaVehicle.setUpgradeRuntimeEnable(true, false)) {
            sleep(3000);
            return false;
        }
        sleep(3000);
        return true;
    }
}
