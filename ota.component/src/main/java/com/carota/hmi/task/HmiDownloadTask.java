package com.carota.hmi.task;

import com.carota.CarotaClient;
import com.carota.core.IDownloadCallback;
import com.carota.core.ISession;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.type.HmiTaskType;
import com.momock.util.Logger;

public final class HmiDownloadTask extends BaseTask implements IDownloadCallback {
    private boolean isSuccess = false;
    private boolean needWait = false;

    public HmiDownloadTask() {
        super();
    }

    @Override
    IHmiCallback.IHmiResult runNode() {
        isSuccess = false;
        needWait = true;
        try {
            boolean download = CarotaClient.startDownload(this);
            if (!download) {
                Logger.info("HMI-Task Start Download Fail @%s", getType());
                needWait = false;
            }
            while (needWait) {
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return new IHmiCallback.IHmiResult(isSuccess);
    }

    @Override
    public HmiTaskType getType() {
        return HmiTaskType.download;
    }

    @Override
    public void onProcess(ISession s) {

    }

    @Override
    public void onFinished(ISession s, boolean success) {
        isSuccess = success;
        needWait = false;
    }
}
