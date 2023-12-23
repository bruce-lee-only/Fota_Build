package com.carota.hmi.action;

import android.os.Handler;

import com.carota.hmi.EventType;
import com.carota.hmi.node.StateMachine;
import com.momock.util.Logger;

public class BaseAction {
    final boolean isSuccess;
    final boolean isAutoRunNextNode;
    final EventType mType;
    final Handler mHandler;

    protected BaseAction(boolean isSuccess, boolean isAutoRunNextNode, EventType type, Handler handler) {
        this.isSuccess = isSuccess;
        this.isAutoRunNextNode = isAutoRunNextNode;
        this.mType = type;
        this.mHandler = handler;
    }

    protected boolean nextAction() {
        if (!isSuccess) {
            Logger.error("HMI Not Run Next Action,Because Action Not Run Success");
        } else if (isAutoRunNextNode) {
            Logger.error("HMI Not Run Next Action,Because Next Action is Auto Run");
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_ACTION_NEXT_NODE));
            return true;
        }
        return false;
    }

    protected boolean executeAgain() {
        if (canExecute()) {
            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_ACTION_RUN_AGAIN));
            return true;
        }
        Logger.error("HMI Not Run Action Again,Because Not Execute");
        return false;
    }

    private boolean canExecute() {
        switch (mType) {
            case ENTER_OTA:
            case DOWNLOAD:
            case CONDITION:
            case CHECK:
            case EXIT_OTA:
                return !isSuccess;
        }
        return false;
    }
}
