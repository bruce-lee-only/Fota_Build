package com.carota.hmi.remote;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.carota.CarotaVehicle;
import com.carota.core.ScheduleAttribute;
import com.carota.hmi.StateMachine;
import com.momock.util.Logger;

/**
 * remote control message
 */
public class RemoteMessage {
    private long mSystemClockTime;
    private long mScheduleTime = -1L;

    public void start(Handler handler) {
        //开启定时查询vsi是否有结果
        //init Schedule or Factory
        startTimeReciver();
        getRemoteEvent(handler);
        new Thread(() -> {
            while (true) {
                try {
                    getRemoteEvent(handler);
                    Thread.sleep(10_000);
                } catch (Exception e) {
                    Logger.error(e);
                }
            }
        }).start();
    }

    private void startTimeReciver() {
//        MyReciver reciver = new MyReciver();
//        CarotaVehicle.registerHandler(reciver);
//        long endTime = SystemClock.elapsedRealtime() + timeout;
//        while (!reciver.init) {
//            Thread.sleep(5000);
//            if (SystemClock.elapsedRealtime() > endTime) {
//                Logger.info("HMI-Init Schedule Fail");
//                break;
//            }
//        }
    }

    private void getRemoteEvent(Handler mHandler) {
        ScheduleAttribute attribute = CarotaVehicle.getSchedule();
        if (attribute != null) {
            switch ((int) attribute.scheduleType) {
                case ScheduleAttribute.TYPE_FACTORY:
                    if (CarotaVehicle.setScheduleIdle()) {
                        Logger.info("HMI-Beat Start Factory @%s", getType());
                        mHandler.sendEmptyMessage(StateMachine.MESSAGE_TYPE_FACTORY);
                    }
                    break;
                case ScheduleAttribute.TYPE_CANCEL:
                    if (CarotaVehicle.setScheduleIdle()) {
                        Logger.info("HMI-Beat Cancle Schedule @%s", getType());
                        mHandler.sendEmptyMessage(StateMachine.MESSAGE_TYPE_CANCLE_TIME);
                    }
                    break;
                case ScheduleAttribute.TYPE_NORMAL:
                    long time = attribute.scheduleTime;
                    //time 不为负
                    if (time == 0L) {
                        if (CarotaVehicle.setScheduleIdle()) {
                            Logger.info("HMI-Beat Upgrade Now @%s", getType());
                            mHandler.sendEmptyMessage(StateMachine.MESSAGE_TYPE_UPGRADE_NOW);
                        }
                    } else if (getSystemClockTime() >= time) {
                        if (CarotaVehicle.setScheduleIdle()) {
                            Logger.info("HMI-Beat Start Schedule Upgrade @%s", getType());
                            mHandler.sendEmptyMessage(StateMachine.MESSAGE_TYPE_SCHEDULE);
                        }
                    } else if (mScheduleTime != time) {
                        Logger.info("HMI-Beat Time Charge To %d @%s", time, getType());
                        mScheduleTime = time;
                        mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_TIME_CHANGE, mScheduleTime));
                    }
            }
        }
    }

    private long getSystemClockTime() {
        return Math.max(mSystemClockTime, System.currentTimeMillis());
    }

    private String getType() {
        return "RemoteMessage";
    }


    private static class MyReciver extends BroadcastReceiver {

        boolean init = false;
        private long mSystemClockTime;

        @Override
        public void onReceive(Context context, Intent intent) {
//            init = true;
            if (intent != null && "BEAT".equals(intent.getStringExtra("action"))) {
                mSystemClockTime = intent.getLongExtra("systemClock", -1);
            } else {
                //defult callback
//                postRunable(() -> mCallBack.schedule().onScheduleReceive(context, intent, mStatus.getSession()));
            }
        }
    }
}
