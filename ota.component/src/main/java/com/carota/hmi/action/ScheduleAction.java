package com.carota.hmi.action;

import com.carota.hmi.UpgradeType;
import com.carota.hmi.StateMachine;

public class ScheduleAction implements IAction {
    private final StateMachine mStateMachine;

    public ScheduleAction(StateMachine stateMachine) {
        this.mStateMachine = stateMachine;
    }

    @Override
    public void start() {
        mStateMachine.start(UpgradeType.SCHEDULE);
    }

    @Override
    public void cancal() {
        mStateMachine.cancle(UpgradeType.SCHEDULE);
    }
}
