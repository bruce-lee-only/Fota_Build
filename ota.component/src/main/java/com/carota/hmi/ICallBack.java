package com.carota.hmi;

import android.content.Context;
import android.content.Intent;

import com.carota.core.ISession;
import com.carota.hmi.action.OperationAction;
import com.carota.hmi.callback.ICheck;
import com.carota.hmi.callback.ICondition;
import com.carota.hmi.callback.IDownLoad;
import com.carota.hmi.callback.IEnterOta;
import com.carota.hmi.callback.IExitOta;
import com.carota.hmi.callback.IInit;
import com.carota.hmi.callback.IInstall;
import com.carota.hmi.callback.IRescue;

public interface ICallBack {
    //no error
    int STATE_NO_ERROR = 0;
    int STATE_USER_REFUSE = 0;
    int STATE_NO_TASK = 2;
    int STATE_DOWNLOAD_FAIL = 3;
    int STATE_ENTER_OTA_FAIL = 4;
    int STATE_TASK_VERIFY_FAIL = 5;

    //condition
    int STATE_CONDITION_SPEED_FAIL = 6;
    int STATE_CONDITION_BATTERY_VOLTAGE_FAIL = 7;
    int STATE_CONDITION_POWER_FAIL = 8;
    int STATE_CONDITION_ENGINE_FAIL = 9;
    int STATE_CONDITION_MOTOR_FAIL = 10;
    int STATE_CONDITION_GEAR_FAIL = 11;
    int STATE_CONDITION_HANDBRAKE_FAIL = 12;
    int STATE_CONDITION_CHARGING_FAIL = 13;
    int STATE_CONDITION_ASS_FAIL = 14;
    int STATE_CONDITION_BATTERY_POWER_FAIL = 15;
    int STATE_CONDITION_DIAGNOSE_FAIL = 16;

    int STATE_UPGRADE_IDLE = 17;
    int STATE_UPGRADE_UPGRADING = 18;
    int STATE_UPGRADE_SUCCESS = 19;
    int STATE_UPGRADE_ROLLBACKING = 20;
    int STATE_UPGRADE_ROLLBACK_SUCCESS = 21;
    int STATE_UPGRADE_ROLLBACK_FAILURE = 22;


    boolean canUpgradeNow();

    IInit init();

    IFactory factory();

    ISchedule schedule();

    ICheck check();

    IDownLoad down();

    IEnterOta enterOta();

    ICondition condition();

    IInstall install();

    IExitOta exitOta();

    IRescue rescue();

//    boolean canStartFactory();

//    boolean canScheduleUpgrade();


//    void onStart(EventType type, ISession s);
//
//    void onDownloading(ISession s, int pro, String speed);
//
//    void onInstallProgressChanged(ISession s, int state, int successCount);
//
//    void onStop(EventType type, boolean success, ISession s, BaseAction action, int state);

//    void onScheduleCancle();
//
//    void onScheduleTimeChange(long time);
//
//    void onScheduleReceive(Context context, Intent intent, ISession mSession);
//
//    void onFactoryStart();
//
//    void onFactoryStop(boolean success, int state);

    //    interface ICommon {
//        void onStart(EventType type, ISession s);
//
//        void onDownloading(ISession s, int pro, String speed);
//
//        void onInstallProgressChanged(ISession s, int state, int successCount);
//
//        void onInstallResult(ISession s, int state);
//
//        void onStop(EventType type, boolean success, ISession s, HmiAction action, int state);
//    }
//
//    interface IUpgradeNow extends ICommon {
//        boolean canUpgradeNow();
//    }
//
    interface ISchedule {
        boolean canScheduleUpgrade(OperationAction action);

        void onScheduleCancle();

        void onScheduleTimeChange(long time);

        void onScheduleReceive(Context context, Intent intent, ISession mSession);

    }

    //
    interface IFactory {
        boolean canStartFactory();

        void onFactoryStart();

        void onFactoryStop(boolean success, int state);
    }
}
