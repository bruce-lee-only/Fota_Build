package com.carota.hmi;

import android.content.Context;

import com.carota.core.ISession;
import com.carota.hmi.callback.ICall;
import com.carota.hmi.callback.ICallBack;
import com.carota.hmi.callback.IDownloadCall;
import com.carota.hmi.callback.IExitOtaCall;
import com.carota.hmi.callback.IInstallCall;
import com.momock.util.Logger;

public class CarOtaHmi {
    private static StateMachine mMachine;

    public static void init(Context context, ICallBack callback, long bootTimeout) {
        if (mMachine != null) {
            Logger.info("HMI Not Init Again");
            return;
        }
        Logger.info("HMI Init");
        mMachine = new StateMachine(context, callback, bootTimeout);
    }

    /**
     * 检测任务
     */
    public static void check(UpgradeType type, ICall callback) {
        mMachine.runNode(type, EventType.CHECK,callback);
    }

    /**
     * 下载任务
     */
    public static void download(UpgradeType type, IDownloadCall callback) {
        mMachine.runNode(type, EventType.DOWNLOAD, callback);
    }


    /**
     * set remote upgrade time
     *
     * @param type
     * @param time -1:Cancle time
     *             >0:set remote time
     */
    public static void setTime(UpgradeType type, long time, ICall callback) {
        mMachine.setTime(type, time, callback);
    }


    public static void vehicleCondition(UpgradeType type, ICall callback) {
        mMachine.vehicleCondition(type, callback);
    }

    public static void enableOtaMode(UpgradeType type, ICall callback) {
        mMachine.runNode(type, EventType.ENTER_OTA, callback);
    }

    public static void disableOtaMode(UpgradeType type, IExitOtaCall callback) {
        mMachine.runNode(type, EventType.EXIT_OTA, callback);
    }

    public static void verifyTaskTimeOut(UpgradeType type, ICall callback) {
        mMachine.runTaskVerifyNode(type, callback);
    }

    public static void install(UpgradeType type, IInstallCall callback) {
        mMachine.runNode(type, EventType.INSTALL, callback);
    }
}
