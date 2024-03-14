package com.carota.hmi.task;

import com.carota.CarotaClient;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.type.HmiTaskType;
import com.momock.util.Logger;

public final class HmiTaskVerifyTask extends BaseTask {

    public HmiTaskVerifyTask() {
        super();
    }

    @Override
    IHmiCallback.IHmiResult runNode() {
        boolean isSuccess = false;
        try {
            isSuccess = CarotaClient.confirmUpdateValid();
        } catch (Exception e) {
            Logger.error(e);
        }

        return new IHmiCallback.IHmiResult(isSuccess);
    }

    @Override
    public HmiTaskType getType() {
        return HmiTaskType.task_timeout_verify;
    }
}
