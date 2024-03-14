package com.carota.hmi.dispacther;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.NetworkOnMainThreadException;

import com.carota.CarotaClient;
import com.carota.CarotaVehicle;
import com.carota.core.ISession;
import com.carota.core.VehicleCondition;
import com.carota.core.report.Event;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.callback.IHmiPolicyManager;
import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;
import com.momock.util.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class UserManageDispacher extends CallbackDispacher implements IHmiPolicyManager {

    private static final int MSG_TAG = 13579;
    private static final String SPEED = "0 KB/S";
    protected final ExecutorService mThreadPool;
    private Handler mHanadler;
    private String downloadSpeed;
    private int downloadPro;

    public UserManageDispacher(Context context, IHmiCallback callback) {
        super(context, callback);
        mThreadPool = Executors.newFixedThreadPool(5);
    }

    @Override
    public ISession getSession() {
        return mSession;
    }

    @Override
    public boolean setTime(long time) throws NetworkOnMainThreadException {
        if (Looper.myLooper() == Looper.getMainLooper()) throw new NetworkOnMainThreadException();
        if (getUpgradeType() == UpgradeType.FACTORY) {
            Logger.info("HMI-User Set Time %d is error,because Factory Policy is Run @%s", time, getUpgradeType());
            return false;
        }
        //todo 这里需要考虑是否下载任务
        try {
            boolean schedule = CarotaVehicle.setScheduleUpgrade(time);
            Logger.info("HMI-User Set Time %d is %s @%s", time, schedule ? "Success" : "Fail", getUpgradeType());
            return schedule;
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

    @Override
    public VehicleCondition getCondition() throws NetworkOnMainThreadException {
        if (Looper.myLooper() == Looper.getMainLooper()) throw new NetworkOnMainThreadException();
        Logger.info("HMI-User Get Condition @%s", getUpgradeType());
        return CarotaVehicle.queryVehicleCondition();
    }

    @Override
    public void sendEvent(int type, int eventCode) {
        Logger.info("HMI-User Send Event @%s", getUpgradeType());
        mThreadPool.execute(() -> CarotaClient.sendUiEvent(type, eventCode, null, Event.Result.RESULT_SUCCESS));
    }

    @Override
    public void taskStart(HmiTaskType type) {
        super.taskStart(type);
        if (type == HmiTaskType.download && mHanadler == null) {
            mHanadler = new MyHandler();
            mHanadler.sendEmptyMessage(MSG_TAG);
        }
    }

    @Override
    public void taskEnd(HmiTaskType type, IHmiCallback.IHmiResult result) {
        super.taskEnd(type, result);
        if (type == HmiTaskType.download) {
            if (mHanadler != null) {
                mHanadler.removeMessages(MSG_TAG);
            }
            if (result.isSuccess()) {
                downloadPro = 100;
                downloadSpeed = SPEED;
            }
        }
    }

    @Override
    public int getTotalDownloadPro() {
        return downloadPro;
    }

    @Override
    public String getTotalDownloadSpeed() {
        return downloadSpeed;
    }


    private String getDownloadSpeed() {
        ISession session = getSession();
        String speed = SPEED;
        if (session == null || session.getTaskCount() == 0) return speed;
//        if (getUpgradeType() == UpgradeType.SCHEDULE || getUpgradeType() == UpgradeType.PUSH_UPGRADE) {
//            return speed;
//        }
        int count = session.getTaskCount();
        long speedLength = 0L;
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                speedLength += session.getTask(i).getDownloadSpeed();
            }
        }
        long kbs = speedLength >> 10;
        if (kbs >> 10 > 0) {
            speed = String.format("%.1f MB/S", kbs / 1024f);
        } else {
            speed = String.format("%d KB/S", kbs);
        }
        return speed;
    }

    private int getDownloadPro() {
        ISession session = getSession();
        if (session == null || session.getTaskCount() == 0) return 0;
//        if (getUpgradeType() == UpgradeType.SCHEDULE || getUpgradeType() == UpgradeType.PUSH_UPGRADE) {
//            return 100;
//        }
        int totalProgress = 0;
        int taskCount = session.getTaskCount();
        for (int i = 0; i < taskCount; i++) {
            totalProgress += session.getTask(i).getDownloadProgress();
        }
        return totalProgress / taskCount;
    }

    private class MyHandler extends Handler {

        public MyHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            downloadSpeed = getDownloadSpeed();
            downloadPro = getDownloadPro();
            if (mHanadler != null) mHanadler.sendEmptyMessageDelayed(MSG_TAG, 1000);
        }
    }
}
