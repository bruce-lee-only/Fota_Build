package com.carota.hmi.node;

import android.os.Handler;

import com.carota.CarotaVehicle;
import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.callback.IExitOtaCall;
import com.carota.hmi.status.HmiStatus;

/**
 * Exit Ota
 */
class ExitOtaNode extends BaseNode {
    ExitOtaNode(HmiStatus hmiStatus, Handler handler, CallBackManager callback) {
        super(hmiStatus, handler, callback);
    }

    @Override
    public EventType getType() {
        return EventType.EXIT_OTA;
    }

    @Override
    protected boolean execute() {
        boolean inOta = true;
        if (CarotaVehicle.setUpgradeRuntimeEnable(false, false)) {
            inOta = ((IExitOtaCall) mCallBack.getICall(getType())).inOta();
        }
        return !inOta;
    }
}
