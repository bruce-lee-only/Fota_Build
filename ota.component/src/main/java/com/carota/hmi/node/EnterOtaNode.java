package com.carota.hmi.node;

import com.carota.CarotaVehicle;
import com.carota.hmi.EventType;
import com.carota.hmi.action.EnterOtaAction;

/**
 * enter ota
 */
class EnterOtaNode extends BaseNode {
    EnterOtaNode(StateMachine status) {
        super(status);
    }

    @Override
    void onStart() {
        mCallBack.enterOta().onStart();
    }

    @Override
    void onStop(boolean success) {
        mCallBack.enterOta().onStop(success, new EnterOtaAction(success,isAutoRunNextNode(),mHandler));
    }

    @Override
    public EventType getType() {
        return EventType.ENTER_OTA;
    }

    @Override
    protected boolean execute() {
        if (!CarotaVehicle.setUpgradeRuntimeEnable(true, false)) {
            sleep(3000);
            mStatus.saveInOta(false);
            return false;
        }
        mStatus.saveInOta(true);
        sleep(3000);
        return true;
    }
}
