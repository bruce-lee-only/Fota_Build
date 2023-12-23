package com.carota.hmi.action;

import android.os.Handler;

import com.carota.hmi.EventType;
import com.carota.hmi.node.StateMachine;
import com.momock.util.Logger;

public class ConditionAction extends BaseAction {
    public ConditionAction(boolean isSuccess, boolean isAutoRunNextNode, Handler handler) {
        super(isSuccess, isAutoRunNextNode, EventType.CONDITION, handler);
    }

    public boolean runAgain() {
        return executeAgain();
    }

    public boolean installIfActionSuccess() {
        return nextAction();
    }

    public boolean exitOta() {
        mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_ACTION_EXIT_OTA));
        return true;
    }
}
