package com.carota.hmi.node;

import android.os.Handler;

import com.carota.CarotaVehicle;
import com.carota.hmi.EventType;

/**
 * Exit Ota
 */
class ExitOtaNode extends BaseNode {
    ExitOtaNode(StateMachine status) {
        super(status);
    }

    @Override
    void onStart() {
        mCallBack.exitOta().onStart();
    }

    @Override
    void onStop(boolean success) {
        mCallBack.exitOta().onStop(success);
    }

    @Override
    public EventType getType() {
        return EventType.EXIT_OTA;
    }

    @Override
    protected boolean execute() {
        //todo: mode by lipiyan for 2023-06-16 退ota失败结果回调只做一次，后续进行持续退出操作
        boolean result = true;
        boolean send = false;
        while (result) {
            result = !CarotaVehicle.setUpgradeRuntimeEnable(false, false);
            if (!send) {
                mCallBack.exitOta().onResult(!result);
                send = true;
            }
            sleep(5000);
        }
        sleep(3000);
        mStatus.saveInOta(false);
        return true;
        //todo: mode by lipiyan 2023-06-16 for 退ota失败结果回调只做一次，后续进行持续退出操作
    }
}
