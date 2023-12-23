package com.carota.hmi;

import android.content.Context;

import com.carota.hmi.node.StateMachine;
import com.momock.util.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

public class CarOtaHmi {
    private static final AtomicBoolean isRun = new AtomicBoolean(false);

    public static void init(Context context, ICallBack callback, long bootTimeout) {
        if (isRun.get()) {
            Logger.info("HMI Not Init Again");
            return;
        }
        isRun.set(true);
        Logger.info("HMI Init");
        new StateMachine.Bulider()
                .addEventQueue()
                .addFactoryQueue()
                .addSchduleQueue()
                .addUpgradeNowQueue()
                .bulid(context, callback)
                .start(bootTimeout);
    }
}
