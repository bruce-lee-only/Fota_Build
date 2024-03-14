package com.carota.hmi.task;

import android.content.Context;

import com.carota.hmi.exception.CarOtaHmiBulidException;
import com.carota.hmi.task.callback.ITask;
import com.carota.hmi.task.callback.ITaskDataCallback;
import com.carota.hmi.task.callback.InitTaskCallback;
import com.carota.hmi.type.HmiTaskType;
import com.momock.util.Logger;

import java.util.LinkedList;

public class TaskFactory {


    private TaskFactory() {
    }

    public static ITask getRootTask(LinkedList<HmiTaskType> types, Context context, ITaskDataCallback callback) {
        BaseTask root = null;
        Logger.info("HMI-P BTask Create List is %s", types);
        for (HmiTaskType type : types) {
            if (root == null) {
                root = getHmiTask(type, context, callback);
            } else {
                root.addNext(getHmiTask(type, context, callback));
            }
        }
        return root;
    }


    private static BaseTask getHmiTask(HmiTaskType type, Context context, ITaskDataCallback callback) {
        switch (type) {
            case check:
                return new HmiCheckTask(callback);
            case download:
                return new HmiDownloadTask();
            case enter_ota:
                return new HmiEnterOtaTask();
            case install:
                return new HmiInstallTask(context, callback);
            case exit_ota:
                return new HmiExitOtaTask();
            case condition:
                return new HmiConditionTask();
            case task_timeout_verify:
                return new HmiTaskVerifyTask();
            case wait_user_run_next:
                return new HmiUserWaitTask(callback);
            default:
                throw new CarOtaHmiBulidException("Hmi Not Create The Task " + type);
        }
    }

    public static Runnable getInit(Context context, long timeOut, boolean needRunRemote, InitTaskCallback callback) {
        return new InitTask(context, timeOut, needRunRemote, callback);
    }

    public static BaseTask getExitOta() {
        return new HmiExitOtaTask();
    }

//    public int getIndex(HmiTaskType type) {
//        return mTaskTypeList.indexOf(type);
//    }
}
