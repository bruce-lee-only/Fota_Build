package com.carota.hmi.node;

import android.os.Handler;

import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.callback.ICallBack;
import com.carota.hmi.status.HmiStatus;
import com.carota.hmi.status.UpgradeStaus;
import com.momock.util.Logger;


abstract class BaseNode implements INode {
    protected final HmiStatus mStatus;
    final CallBackManager mCallBack;
    final Handler mHandler;

    BaseNode(HmiStatus hmiStatus, Handler handler, CallBackManager callback) {
        mCallBack = callback;
        mHandler = handler;
        mStatus = hmiStatus;
    }

    protected abstract boolean execute();

    @Override
    public final Boolean call() {
        Logger.info("Hmi start run Node @1s", getType());
        mStatus.setUpgradeStatus(getType(), UpgradeStaus.RUNNING);
        if (getType() != EventType.INSTALL) {
            mHandler.post(() -> mCallBack.getICall(getType()).onStart(mStatus.getUpgradeType()));
        }
        boolean result = execute();
        Logger.info("Hmi Node run End,Result:%1b @2s ", result, getType());
        if (getType() != EventType.INSTALL || !result) {
            mStatus.setUpgradeStatus(getType(), result ? UpgradeStaus.SUCCESS : UpgradeStaus.FAIL);
            mHandler.postDelayed(() -> mCallBack.getICall(getType()).onEnd(mStatus.getUpgradeType(), result, mStatus), 1000);
            mCallBack.removeCallBack(getType());
        }
        return result;
    }

    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Logger.error(e);
        }
    }

}
