package com.carota.hmi.action;

import com.carota.core.VehicleCondition;
import com.carota.hmi.IExitOtaResult;
import com.carota.hmi.IResult;
import com.carota.hmi.ISchedule;
import com.carota.hmi.ITaskVerifyResult;
import com.carota.hmi.callback.ICheck;
import com.carota.hmi.callback.IInstall;
import com.carota.hmi.node.StateMachine;

public class OperationAction{

    private final StateMachine mState;

    public OperationAction(StateMachine state) {
        this.mState = state;
    }

    public void taskVerify(IResult callback) {
        mState.runTaskVerifyNode(callback);
    }

    public VehicleCondition vehicleCondition() {
        return mState.runVehicleConditionNode(null);
    }

    public void vehicleCondition(ITaskVerifyResult callback) {
        mState.vehicleCondition(callback);
    }

    //todo: add by lipiyan 2023-03-14
    public void exitOtaModel(IExitOtaResult callback) {
        mState.exitOtaModel(callback);
    }
    //todo: add by lipiyan 2023-03-14

    //todo: add by lipiyan 2023-06-24
    public void installUpdate(IInstall callback) {
        mState.installUpdate(callback);
    }
    //todo: add by lipiyan 2023-06-24

    public void cancleTime(String tid, ISchedule callback) {
        setTime(-1, tid, callback);
    }

    public void setTime(long time, String tid, ISchedule callback) {
        mState.setTime(time, tid, callback);
    }
}
