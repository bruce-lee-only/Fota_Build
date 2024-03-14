package com.carota.hmi.task;

import android.content.Context;

import com.carota.CarotaClient;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.task.callback.ITaskDataCallback;
import com.carota.hmi.type.HmiTaskType;
import com.momock.util.Logger;

public final class HmiInstallTask extends BaseTask {
    private final Context mContext;
    private ITaskDataCallback mCallback;

    public HmiInstallTask(Context mContext, ITaskDataCallback callback) {
        super();
        this.mContext = mContext;
        mCallback = callback;
    }

    @Override
    IHmiCallback.IHmiResult runNode() {
        try {
            mCallback.saveInstallType();
            if (CarotaClient.getClientStatus().isUpgradeTriggered()) {
                return new IHmiCallback.IHmiResult(true);
            } else if (CarotaClient.install(mContext, true)) {
                return new IHmiCallback.IHmiResult(true);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return new IHmiCallback.IHmiResult(false);
    }

    @Override
    public HmiTaskType getType() {
        return HmiTaskType.install;
    }
}
