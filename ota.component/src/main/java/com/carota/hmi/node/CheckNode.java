package com.carota.hmi.node;

import android.content.res.Configuration;
import android.os.Bundle;

import com.carota.CarotaClient;
import com.carota.core.ISession;
import com.carota.hmi.EventType;
import com.carota.hmi.action.CheckAction;
import com.momock.util.Logger;

/**
 * check task
 */
class CheckNode extends BaseNode {

    CheckNode(StateMachine status) {
        super(status);
    }

    @Override
    void onStart() {
        mCallBack.check().onStart();
    }

    @Override
    void onStop(boolean success) {
        mCallBack.check().onStop(success, mStatus.getSession(), new CheckAction(success, isAutoRunNextNode(), mHandler));
    }

    @Override
    public EventType getType() {
        return EventType.CHECK;
    }

    @Override
    protected boolean execute() {
        try {
            Logger.debug("CheckNode isFactory?:" + (mStatus.isFactory() || mStatus.factoryRun));
            boolean isFactory = mStatus.factoryRun || mStatus.isFactory();
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFactory", isFactory);
            bundle.putString("lang", mCallBack.check().language());
            Logger.info("current language: " + mCallBack.check().language());

            if (mStatus.isFactory()) onStart();

            ISession session;
            session = CarotaClient.check(bundle, null);
            mStatus.setSession(session);
            if (session != null && session.getTaskCount() > 0) {
                if (mStatus.isFactory()) onStop(true);
                return true;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
//        setErrorCode(ICallBack.STATE_NO_TASK);
        if (mStatus.isFactory()) onStop(false);
        return false;
    }

}
