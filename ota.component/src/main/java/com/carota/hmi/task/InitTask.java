package com.carota.hmi.task;

import android.content.Context;
import android.os.SystemClock;

import com.carota.CarotaClient;
import com.carota.CarotaVehicle;
import com.carota.core.IInstallViewHandler;
import com.carota.core.ScheduleAttribute;
import com.carota.hmi.task.callback.InitTaskCallback;
import com.carota.hmi.type.UpgradeType;
import com.momock.util.Logger;

public class InitTask implements Runnable, CarotaClient.IInstallViewHandlerFactory {
    private final long mTimeOut;
    private final InitTaskCallback mCallback;
    private final Context mContext;
    private long mScheduleTime;
    private boolean cancheck;
    private final boolean needRunRemoteMassage;

    public InitTask(Context context, long timeout, boolean needRunRemote, InitTaskCallback callback) {
        this.mTimeOut = timeout;
        this.needRunRemoteMassage = needRunRemote;
        this.mCallback = callback;
        this.mContext = context;
    }

    @Override
    public void run() {
        cancheck = true;
        long endTime = mTimeOut + SystemClock.elapsedRealtime();
        Logger.info("HMI-Task Start `init`");
        try {
            //init sdk
            CarotaClient.init(mContext, this, mTimeOut);
            if (CarotaClient.getClientStatus().isUpgradeTriggered()) {
                cancheck = false;
            }
            mCallback.updateSession(CarotaClient.getClientSession());
            //固定退ota,暂时先不处理
            if (needRunRemoteMassage) {
                Logger.info("HMI-Task `init` Task Start Run Remote Ctrl");
                startRemote(endTime);
                Logger.info("HMI-Task `init` Task Run Remote Ctrl End");
            } else {
                Logger.info("HMI-Task `init` Task Not Run Remote Ctrl");
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        Logger.info("HMI-Task `init` Task End ,%s Check", cancheck ? "Can" : "Cannot");
        mCallback.initEnd(cancheck);
    }

    private void startRemote(long endTime) {
        //开启定时查询vsi是否有结果
        do {
            try {
                if (getRemoteEvent()) {
                    break;
                }
                Thread.sleep(10_000);
            } catch (Exception e) {
                Logger.error(e);
            }
        } while (SystemClock.elapsedRealtime() < endTime);

        new Thread(() -> {
            while (true) {
                try {
                    getRemoteEvent();
                    Thread.sleep(60_000);
                } catch (Exception e) {
                    Logger.error(e);
                }
            }
        }).start();
    }

    private boolean getRemoteEvent() {
        ScheduleAttribute attribute = CarotaVehicle.getSchedule();
        if (attribute != null) {
            switch ((int) attribute.scheduleType) {
                case ScheduleAttribute.TYPE_FACTORY:
                    if (CarotaVehicle.setScheduleIdle()) {
                        Logger.info("HMI-Task `beat` Start Factory");
                        mCallback.findRemoteUprade(UpgradeType.FACTORY);
                    } else {
                        Logger.info("HMI-Task `beat` Start Factory,But Set Idle Fail");
                    }
                    cancheck = false;
                    break;
                case ScheduleAttribute.TYPE_CANCEL:
                    if (CarotaVehicle.setScheduleIdle()) {
                        Logger.info("HMI-Task `beat` Cancle Schedule");
                        mScheduleTime = 0;
                        mCallback.findTimeChange(-1);
                    } else {
                        Logger.info("HMI-Task `beat` Cancle Schedule,But Set Idle Fail");
                    }
                    cancheck = false;
                    break;
                case ScheduleAttribute.TYPE_NORMAL:
                    long time = attribute.scheduleTime;
                    //time 不为负
                    if (time == 0L) {
                        if (CarotaVehicle.setScheduleIdle()) {
                            Logger.info("HMI-Task `beat` Start Push Upgrade");
                            mCallback.findRemoteUprade(UpgradeType.PUSH_UPGRADE);
                        } else {
                            Logger.info("HMI-Task `beat` Start Push Upgrade,But Set Idle Fail");
                        }
                        cancheck = false;
                    } else if (System.currentTimeMillis() >= time) {
                        if (CarotaVehicle.setScheduleIdle()) {
                            Logger.info("HMI-Task `beat` Start Schedule Upgrade %d,%d", System.currentTimeMillis(), time);
                            mCallback.findRemoteUprade(UpgradeType.SCHEDULE);
                        } else {
                            Logger.info("HMI-Task `beat` Start Schedule Upgrade,But Set Idle Fail");
                        }
                        cancheck = false;
                    } else if (mScheduleTime != time) {
                        //Time charge
                        Logger.info("HMI-Task `beat` Time Charge To %d", time);
                        cancheck = false;
                        mScheduleTime = time;
                        mCallback.findTimeChange(time);
                    }
            }
        }
        return attribute != null;
    }

    @Override
    public IInstallViewHandler create(Context context) {
        return mCallback;
    }

}
