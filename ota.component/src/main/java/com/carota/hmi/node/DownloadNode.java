package com.carota.hmi.node;

import android.os.Handler;

import com.carota.CarotaClient;
import com.carota.core.IDownloadCallback;
import com.carota.core.ISession;
import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.callback.ICall;
import com.carota.hmi.callback.IDownloadCall;
import com.carota.hmi.status.HmiStatus;
import com.momock.util.Logger;


/**
 * Download task
 */
class DownloadNode extends BaseNode {
    private boolean isSuccess;
    private boolean needWait;

    DownloadNode(HmiStatus hmiStatus, Handler handler, CallBackManager callback) {
        super(hmiStatus, handler, callback);
    }

    @Override
    public EventType getType() {
        return EventType.DOWNLOAD;
    }

    @Override
    protected boolean execute() {
        isSuccess = false;
        needWait = true;
        IDownloadCall call = (IDownloadCall) mCallBack.getICall(getType());
        try {
            boolean download = CarotaClient.startDownload(new IDownloadCallback() {
                @Override
                public void onProcess(ISession s) {
                    mStatus.setSession(s);
                    if (!mStatus.isFactory())
                        mHandler.post(() -> call.onDownloading(mStatus.getUpgradeType(), getType(), mStatus));
                }

                @Override
                public void onFinished(ISession s, boolean success) {
                    if (!mStatus.isFactory())
                        mHandler.post(() -> call.onDownloading(mStatus.getUpgradeType(), getType(), mStatus));                    isSuccess = success;
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

}
