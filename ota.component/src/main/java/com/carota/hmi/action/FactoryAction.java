package com.carota.hmi.action;

import com.carota.hmi.UpgradeType;
import com.carota.hmi.StateMachine;

public class FactoryAction  implements IAction {
    private final StateMachine mStateMachine;

    public FactoryAction(StateMachine stateMachine) {
        this.mStateMachine = stateMachine;
    }

    @Override
    public void start() {
        mStateMachine.start(UpgradeType.FACTORY);
    }

    @Override
    public void cancal() {
        mStateMachine.cancle(UpgradeType.FACTORY);
    }
}
