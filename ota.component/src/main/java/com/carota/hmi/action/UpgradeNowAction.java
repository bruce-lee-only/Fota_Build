package com.carota.hmi.action;

import com.carota.hmi.StateMachine;
import com.carota.hmi.UpgradeType;

public class UpgradeNowAction implements IAction {
    private final StateMachine mStateMachine;

    public UpgradeNowAction(StateMachine stateMachine) {
        this.mStateMachine = stateMachine;
    }

    @Override
    public void start() {
        mStateMachine.start(UpgradeType.UPGRADE_NOW);
    }

    @Override
    public void cancal() {
        mStateMachine.cancle(UpgradeType.UPGRADE_NOW);
    }
}
