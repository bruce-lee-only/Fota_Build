package com.carota.hmi.callback;

import com.carota.hmi.EventType;

import java.util.HashMap;
import java.util.Map;

public class CallBackManager {
    private final ICallBack mCallBack;
    private final Map<EventType, ICall> mCallBackMap;

    public CallBackManager(ICallBack callBack) {
        this.mCallBack = callBack;
        mCallBackMap = new HashMap<>();
    }

    public void setCallBack(EventType type, ICall call) {
        if (type == null || call == null) return;
        mCallBackMap.put(type, call);
    }

    public void removeCallBack(EventType type) {
        mCallBackMap.remove(type);
    }

    public ICall getICall(EventType type) {
        ICall call = mCallBackMap.get(type);
        if (call == null) {
            switch (type) {
                case CHECK:
                    return mCallBack.check();
                case EXIT_OTA:
                    return mCallBack.exitOta();
                case INSTALL:
                    return mCallBack.install();
                case CONDITION:
                    return mCallBack.condition();
                case ENTER_OTA:
                    return mCallBack.enterOta();
                case TASK_VERIFY:
                    return mCallBack.taskTimeOut();
                case DOWNLOAD:
                    return mCallBack.download();
                case SET_TIME:
                    return mCallBack.setTime();
            }

        }
        return call;
    }

    public void onInitStart() {
        mCallBack.onInitStart();
    }

    public void onInitEnd() {
        mCallBack.onInitEnd();
    }

    public ICallBack.IFactory factory() {
        return mCallBack.factory();
    }

    public ICallBack.ISchedule schedule() {
        return mCallBack.schedule();
    }

    public ICallBack.IUpgradeNow upgradeNow() {
        return mCallBack.upgradeNow();
    }
}
