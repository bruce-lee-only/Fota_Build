package com.carota.hmi.node;

import android.os.Handler;

import com.carota.CarotaClient;
import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.status.HmiStatus;

/**
 * Verify Task available
 */
public class TaskVerifyNode extends BaseNode {

    public TaskVerifyNode(HmiStatus hmiStatus, Handler handler, CallBackManager callback) {
        super(hmiStatus, handler, callback);
    }

    @Override
    public EventType getType() {
        return EventType.TASK_VERIFY;
    }

    @Override
    protected boolean execute() {
        return CarotaClient.confirmUpdateValid();
    }
}
