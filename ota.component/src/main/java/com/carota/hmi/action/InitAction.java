package com.carota.hmi.action;

import android.os.Handler;

import com.carota.hmi.EventType;
import com.carota.hmi.callback.ICheck;
import com.carota.hmi.node.StateMachine;
import com.momock.util.Logger;

public class InitAction extends BaseAction{

    private final OperationAction mOperationAction;

    public InitAction(StateMachine state, boolean isSuccess, boolean isAutoRunNextNode, Handler handler) {
        super(isSuccess, isAutoRunNextNode,EventType.INIT,handler);
        this.mOperationAction = new OperationAction(state);
    }

    public boolean NextAction(){
        return nextAction();
    }

    public void check(){
        if (!isSuccess) {
            Logger.error("HMI Not Run Check,Because Action Not Run Success");
        } else if (isAutoRunNextNode) {
            Logger.error("HMI Not Run Check,Because Next Action is Auto Run");
        } else {
            Logger.info("HMI Run Check Node");
            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_CHECK));
        }
    }

//    public void getSession
}
