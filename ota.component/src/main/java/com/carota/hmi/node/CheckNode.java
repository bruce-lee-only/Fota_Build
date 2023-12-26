package com.carota.hmi.node;

import android.os.Bundle;
import android.os.Handler;

import com.carota.CarotaClient;
import com.carota.core.ISession;
import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.status.HmiStatus;
import com.momock.util.Logger;

/**
 * check task
 */
class CheckNode extends BaseNode {

    CheckNode(HmiStatus hmiStatus, Handler handler, CallBackManager callback) {
        super(hmiStatus, handler, callback);
    }

    @Override
    public EventType getType() {
        return EventType.CHECK;
    }

    @Override
    protected boolean execute() {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFactory", mStatus.isFactory());
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

}
