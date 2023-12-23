package com.carota.hmi.node;

import android.annotation.SuppressLint;

import com.carota.CarotaClient;
import com.carota.core.IDownloadCallback;
import com.carota.core.ISession;
import com.carota.hmi.EventType;
import com.carota.hmi.action.DownLoadAction;
import com.momock.util.Logger;


/**
 * Download task
 */
class DownloadNode extends BaseNode {
    private boolean isSuccess;
    private boolean needWait;

    DownloadNode(StateMachine status) {
        super(status);
    }

    @Override
    void onStart() {
        mCallBack.down().onStart();
    }

    @Override
    void onStop(boolean success) {
        mCallBack.down().onStop(success, mStatus.getSession(),new DownLoadAction(mStatus, success,isAutoRunNextNode(),mHandler));
    }

    @Override
    public EventType getType() {
        return EventType.DOWNLOAD;
    }

    @Override
    protected boolean execute() {
        isSuccess = false;
        needWait = true;
        try {
            boolean download = CarotaClient.startDownload(new IDownloadCallback() {
                @Override
                public void onProcess(ISession s) {
                    mStatus.setSession(s);
                    if (!mStatus.isFactory())
                        mHandler.post(() -> mCallBack.down().onDownloading(s, getPro(s), getSpeed(s)));
                }

                @Override
                public void onFinished(ISession s, boolean success) {
                    if (!mStatus.isFactory())
                        mHandler.post(() -> mCallBack.down().onDownloading(s, getPro(s), getSpeed(s)));
                    isSuccess = success;
                    needWait = false;
                }
            });
            if (!download) {
                Logger.info("HMI-Node Start Download Fail @%s", getType());
                isSuccess = false;
                needWait = false;
            }
            while (needWait) {
                sleep(5000);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return isSuccess;
    }

    /**
     * 下载进度扩展方法
     */
    private int getPro(ISession session) {
        int totalProgress = 0;
        int count = session.getTaskCount();
        if (count == 0) return 0;
        for (int i = 0; i < count; i++) {
            totalProgress += session.getTask(i).getDownloadProgress();
        }
        return totalProgress / session.getTaskCount();
    }

    /**
     * 下载速度扩展方法
     */
    @SuppressLint("DefaultLocale")
    private String getSpeed(ISession session) {
        int count = session.getTaskCount();
        long speed = 0;
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                speed += session.getTask(i).getDownloadSpeed();
            }
        }
        long kbs = speed >> 10;

        if (kbs >= 1024) {
            return String.format("%.1f MB/S", ((float) kbs) / 1024);
        } else {
            return String.format("%d KB/S", kbs);
        }
    }

}
