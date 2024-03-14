package com.carota.hmi.task;

import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.exception.HmiInterruptedException;
import com.carota.hmi.task.callback.ITaskDataCallback;
import com.carota.hmi.type.HmiTaskType;

/**
 * 暂停升级流程
 */
public final class HmiUserWaitTask extends BaseTask {

    private ITaskDataCallback mCallback;

    HmiUserWaitTask(ITaskDataCallback callback) {
        super();
        mCallback = callback;
    }

    @Override
    IHmiCallback.IHmiResult runNode() throws HmiInterruptedException {
        if (mCallback.suspend()) {
            throw new HmiInterruptedException();
        }
        return new IHmiCallback.IHmiResult(true);
    }

    @Override
    public HmiTaskType getType() {
        return HmiTaskType.wait_user_run_next;
    }

}
