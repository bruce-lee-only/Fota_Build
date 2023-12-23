package com.carota.hmi.action;

import android.os.Handler;

import com.carota.core.VehicleCondition;
import com.carota.hmi.EventType;
import com.carota.hmi.IExitOtaResult;
import com.carota.hmi.IResult;
import com.carota.hmi.ISchedule;
import com.carota.hmi.ITaskVerifyResult;
import com.carota.hmi.callback.IInstall;
import com.carota.hmi.node.INode;
import com.carota.hmi.node.StateMachine;
import com.momock.util.Logger;

public class DownLoadAction extends BaseAction {

    private final OperationAction mOperationAction;

    public DownLoadAction(StateMachine state, boolean isSuccess, boolean isAutoRunNextNode, Handler handler) {
        super(isSuccess, isAutoRunNextNode, EventType.DOWNLOAD, handler);
        this.mOperationAction = new OperationAction(state);
    }

    public boolean runAgain() {
        return executeAgain();
    }

    public boolean autoRunAllAction() {
        return nextAction();
    }

    public void taskVerify(IResult callback) {
        if (isAutoRunNextNode) {
            Logger.error("HMI Not Run Task Verify,Because Next Action is Auto Run");
            callback.result(false);
        } else if (!isSuccess) {
            Logger.error("HMI Not Run Task Verify,Because Action The Action Fail");
            callback.result(false);
        } else if (mType != EventType.DOWNLOAD) {
            Logger.error("HMI Not Run Task Verify,Because Action is Not DOWNLOAD");
            callback.result(false);
        } else {
            mOperationAction.taskVerify(callback);
        }
    }

    public VehicleCondition vehicleCondition() {
        if (isAutoRunNextNode) {
            Logger.error("HMI Not Run Task Vehicle Condition,Because Next Action is Auto Run");
        } else if (!isSuccess) {
            Logger.error("HMI Not Run Task Vehicle Condition,Because Action The Action Fail");
        } else if (mType != EventType.DOWNLOAD) {
            Logger.error("HMI Not Run Task Vehicle Condition,Because Action is Not DOWNLOAD");
        } else {
            return mOperationAction.vehicleCondition();
        }
        return null;
    }

    public void vehicleCondition(ITaskVerifyResult callback) {
        if (isAutoRunNextNode) {
            Logger.error("HMI Not Run Task Vehicle Condition,Because Next Action is Auto Run");
            callback.result(null, null);
        } else if (!isSuccess) {
            Logger.error("HMI Not Run Task Vehicle Condition,Because Action The Action Fail");
            callback.result(null, null);
        } else if (mType != EventType.DOWNLOAD) {
            Logger.error("HMI Not Run Task Vehicle Condition,Because Action is Not DOWNLOAD");
            callback.result(null, null);
        } else {
            Logger.info("HMI Run Task Vehicle Condition,mOperationAction run it");
            mOperationAction.vehicleCondition(callback);
        }
    }

    //todo: add by lipiyan 2023-03-14 for 增加退OTA模式方法，奇瑞5.0校验前置条件之前进OTA模式，此时如果需要退出OTA的话，无法拿到precondition的Action
    public void exitOta(IExitOtaResult callback){
        if (isAutoRunNextNode) {
            Logger.error("HMI Not Run Exit OTA-Model,Because Next Action is Auto Run");
            callback.result(false);
        } else if (!isSuccess) {
            Logger.error("HMI Not Run Exit OTA-Model,Because DownLoadAction run Fail");
            callback.result(false);
        } else if (mType != EventType.DOWNLOAD) {
            Logger.error("HMI Not Run Exit OTA-Model,Because Action is Not DOWNLOAD");
            callback.result(false);
        } else {
            Logger.error("HMI DownLoadAction Run Exit OTA-Model");
            mOperationAction.exitOtaModel(callback);
        }
    }
    //todo: add end

    //todo: add by lipiyan 2023-06-24 for 工程模式使用
    public void installUpdate(){
        Logger.info("HMI DownLoadAction Run install");
        mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_ACTION_INSTALL_NODE));
    }
    //todo: add end

    public void cancleTime(String tid, ISchedule callback) {
        setTime(-1, tid, callback);
    }

    public void setTime(long time, String tid, ISchedule callback) {
        if (time < -1 || time == 0) {
            Logger.error("HMI SetTime Not Run,Because Time is error");
            callback.result(false, time);
        } else if (!isSuccess) {
            Logger.error("HMI SetTime Not Run,Because Action The Action DOWNLOAD Fail");
            callback.result(false, time);
        } else if (mType != EventType.DOWNLOAD) {
            Logger.error("HMI SetTime Not Run,Because Action is Not DOWNLOAD");
            callback.result(false, time);
        } else {
            mOperationAction.setTime(time, tid, callback);
        }
    }

    public boolean reset(Boolean isNext) {
        if (!isNext) {
            Logger.error("HMI Not Run Reset,Because Not running");
        } else if (!isSuccess) {
            Logger.error("HMI Not Run Reset,Because Action Not Run Success");
        } else if (isAutoRunNextNode) {
            Logger.error("HMI Not Run Reset,Because Next Action is Auto Run");
        } else {
            Logger.info("HMI Run Download Node Reset");
            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_ACTION_RESET_NODE));
            return true;
        }
        return false;
    }
}
