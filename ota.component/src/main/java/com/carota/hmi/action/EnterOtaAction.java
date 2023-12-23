package com.carota.hmi.action;

import android.os.Handler;

import com.carota.hmi.EventType;

public class EnterOtaAction extends BaseAction {
    public EnterOtaAction(boolean isSuccess, boolean isAutoRunNextNode, Handler handler) {
        super(isSuccess, isAutoRunNextNode, EventType.ENTER_OTA, handler);
    }

    public boolean runAgain() {
        return executeAgain();
    }

    public boolean conditionIfActionSuccess() {
        return nextAction();
    }
}
