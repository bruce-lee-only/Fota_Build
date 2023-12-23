package com.carota.hmi.action;

import android.os.Handler;

import com.carota.hmi.EventType;

public class RescueAction extends BaseAction {
    public RescueAction(boolean isSuccess, boolean isAutoRunNextNode, Handler handler) {
        super(isSuccess, isAutoRunNextNode,EventType.RESCUE,handler);
    }

    public boolean runAgain() {
        return executeAgain();
    }

    public boolean download() {
        return nextAction();
    }
}
