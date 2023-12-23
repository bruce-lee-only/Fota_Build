package com.carota.hmi.action;

import android.os.Handler;

import com.carota.hmi.EventType;
import com.carota.hmi.IExitOtaResult;
import com.carota.hmi.node.StateMachine;
import com.momock.util.Logger;

public class InstallAction extends BaseAction{

    private final OperationAction mOperationAction;

    public InstallAction(StateMachine state, boolean isSuccess, boolean isAutoRunNextNode, Handler handler) {
        super(isSuccess, isAutoRunNextNode, EventType.INSTALL, handler);
        this.mOperationAction = new OperationAction(state);
    }

    public void exitOta(IExitOtaResult callback){
        if (isAutoRunNextNode) {
            Logger.error("HMI Not Run Exit OTA-Model,Because Next Action is Auto Run");
            callback.result(false);
        } else if (!isSuccess) {
            Logger.error("HMI Not Run Exit OTA-Model,Because Action The Action Fail");
            callback.result(false);
        } else if (mType != EventType.INSTALL) {
            Logger.error("HMI Not Run Exit OTA-Model,Because Action is Not INSTALL");
            callback.result(false);
        } else {
            Logger.error("HMI Run Exit OTA-Model");
            mOperationAction.exitOtaModel(callback);
        }
    }
}
