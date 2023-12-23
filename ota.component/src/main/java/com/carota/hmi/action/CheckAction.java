package com.carota.hmi.action;

import android.os.Handler;

import com.carota.hmi.EventType;
import com.carota.hmi.node.StateMachine;
import com.momock.util.Logger;

public class CheckAction extends BaseAction {
    public CheckAction(boolean isSuccess, boolean isAutoRunNextNode, Handler handler) {
        super(isSuccess, isAutoRunNextNode,EventType.CHECK,handler);
    }

    public boolean runAgain() {
        return executeAgain();
    }

    public boolean checkTask(){
        Logger.info("HMI Run Check Node checkTask");
        mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_CHECK));
        return true;
    }

    public boolean download() {
        return nextAction();
    }
}
