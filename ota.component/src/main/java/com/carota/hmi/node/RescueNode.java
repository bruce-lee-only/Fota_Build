package com.carota.hmi.node;

import android.os.Bundle;

import com.carota.CarotaClient;
import com.carota.core.ISession;
import com.carota.hmi.EventType;
import com.carota.hmi.action.RescueAction;
import com.momock.util.Logger;

public class RescueNode extends BaseNode{

    RescueNode(StateMachine status) {
        super(status);
    }

    @Override
    protected boolean execute() {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isRescue", true);
            ISession session = CarotaClient.check(bundle, null);
            mStatus.setSession(session);
            if (session != null && session.getTaskCount() > 0) {
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    void onStart() {
        mCallBack.rescue().onStart();
    }

    @Override
    void onStop(boolean success) {
        mCallBack.rescue().onStop(success, new RescueAction(success, isAutoRunNextNode(), mHandler));
    }

    @Override
    public EventType getType() {
        return EventType.RESCUE;
    }
}
