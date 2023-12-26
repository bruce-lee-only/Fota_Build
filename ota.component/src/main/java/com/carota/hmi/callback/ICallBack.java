package com.carota.hmi.callback;

import android.content.Context;
import android.content.Intent;

import com.carota.core.ISession;
import com.carota.hmi.action.FactoryAction;
import com.carota.hmi.action.ScheduleAction;
import com.carota.hmi.action.UpgradeNowAction;
import com.carota.hmi.status.IStatus;

public interface ICallBack {
    void onInitStart();

    void onInitEnd();

    IFactory factory();

    ISchedule schedule();

    IUpgradeNow upgradeNow();


    ICall check();

    ICall download();

    ICall enterOta();

    ICall condition();

    ICall install();

    IExitOtaCall exitOta();

    ICall taskTimeOut();

    ICall setTime();

    interface ISchedule extends IRemote {
        void onScheduleUpgrade(ScheduleAction action);

        void onScheduleTimeChange(long time);

        void onScheduleReceive(Context context, Intent intent, ISession mSession);

        void onScheduleTimeCancle();
    }

    //
    interface IFactory extends IRemote {
        void onFactory(FactoryAction factoryAction);


    }

    interface IUpgradeNow extends IRemote {
        void onUpgradeNow(UpgradeNowAction action);

    }

    interface IRemote {
        void onStart();

        void onError(int error);
        void onCancle();

        void onStop(boolean success, IStatus status);
    }

}
