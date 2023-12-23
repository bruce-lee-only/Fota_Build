package com.carota.hmi.node;

import com.carota.CarotaClient;
import com.carota.hmi.EventType;

/**
 * Verify Task available
 */
public class TaskVerifyNode extends BaseNode {

    public TaskVerifyNode(StateMachine status) {
        super(status);
    }

    @Override
    void onStart() {

    }

    @Override
    void onStop(boolean success) {

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
