package com.carota.hmi.task;

import android.os.Bundle;

import com.carota.CarotaClient;
import com.carota.core.ISession;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.task.callback.ITaskDataCallback;
import com.carota.hmi.type.HmiTaskType;
import com.momock.util.Logger;

public final class HmiCheckTask extends BaseTask {
    private final ITaskDataCallback mCallback;

    public HmiCheckTask(ITaskDataCallback callback) {
        super();
        mCallback = callback;
    }

    @Override
    IHmiCallback.IHmiResult runNode() {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean("isFactory", mCallback.isFactory());
            ISession session = CarotaClient.check(bundle, null);
            if (session != null && session.getTaskCount() > 0) {
                return new IHmiCallback.IHmiResult(true);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return new IHmiCallback.IHmiResult(false);
    }

    @Override
    public HmiTaskType getType() {
        return HmiTaskType.check;
    }

}
