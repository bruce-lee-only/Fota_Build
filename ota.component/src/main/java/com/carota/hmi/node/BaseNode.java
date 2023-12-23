package com.carota.hmi.node;

import android.os.Handler;

import com.carota.core.VehicleCondition;
import com.carota.hmi.EventType;
import com.carota.hmi.ICallBack;
import com.carota.hmi.callback.ICondition;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;


abstract class BaseNode implements INode {
    protected final StateMachine mStatus;
    private boolean isAutoRunNextNode;
    final ICallBack mCallBack;
    private boolean isSuccess;
    final Handler mHandler;

    BaseNode(StateMachine status) {
        this.mStatus = status;
        mCallBack = mStatus.getCallback();
        mHandler = mStatus.getHandler();
    }

    protected abstract boolean execute();

    abstract void onStart();

    abstract void onStop(boolean success);

    @Override
    public boolean runNode() {
        isSuccess = false;
        mHandler.post(this::onStart);
        sleep(100);
        Logger.debug("lipiyan run node execute");
        isSuccess = execute();
        sleep(100);
        Logger.debug("lipiyan run node onStop");
        mHandler.post(() -> onStop(isSuccess));
        return isSuccess;
    }

    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Logger.error(e);
        }
    }

    @Override
    public final boolean isAutoRunNextNode() {
        return isAutoRunNextNode;
    }

    VehicleCondition getVehicleCondition() {
        return null;
    }

    //todo: add by lipiyan 2022-02-22
    List<ICondition.IConditionItem> getVerifyResult() {
        return null;
    }

    void setExtraCondition(ArrayList<VehicleCondition.Precondition> extraCondition){}
    //todo: add by lipiyan 2022-02-22

    final void setAutoRunNextNode(boolean autoRunNextNode) {
        Logger.info("HMI-Node Set AutoRunNextNode: %b @%s", autoRunNextNode, getType());
        isAutoRunNextNode = autoRunNextNode;
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

}
