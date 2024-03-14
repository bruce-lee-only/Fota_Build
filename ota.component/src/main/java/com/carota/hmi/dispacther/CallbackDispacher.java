package com.carota.hmi.dispacther;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.carota.CarotaClient;
import com.carota.core.ISession;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.callback.IHmiPolicyManager;
import com.carota.hmi.db.HmiDbManager;
import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.task.callback.InitTaskCallback;
import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;
import com.momock.util.Logger;

public abstract class CallbackDispacher extends BaseDataDispacher implements InitTaskCallback, ITaskCallback {
    private final HmiDbManager mHmiDbManager;
    private final IHmiCallback mHmiCallback;
    protected ISession mSession;
    private final Handler mMainHandler;

    public CallbackDispacher(Context context, IHmiCallback callback) {
        super(context);
        mHmiDbManager = new HmiDbManager(context);
        mHmiCallback = callback;
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public final boolean onInstallStart(ISession s) {
        Logger.info("HMI-D IView Start");
        updateSession(s);
        UpgradeType type = mHmiDbManager.getInstallPolicyType();
        return installStart(type);
    }

    abstract boolean installStart(UpgradeType type);

    @Override
    public boolean onInstallStop(ISession s, int state) {
        taskEnd(HmiTaskType.install, new IHmiCallback.IHmiResult(true, state));
        return false;
    }

    @Override
    public final void onInstallProgressChanged(ISession s, int state, int successCount) {
        // TODO: 2024/1/31 nothing
    }

    @Override
    public void updateSession(ISession session) {
        mSession = session;
    }

    @Override
    public void initEnd(boolean cancheck) {
        if (cancheck) {
            mMainHandler.postDelayed(() -> mHmiCallback.updatePolicyManager((IHmiPolicyManager) this), 100);
            mMainHandler.postDelayed(() -> mHmiCallback.startRunPolicy(UpgradeType.DEFULT), 100);
        }
    }

    final void sendStartPolicy(boolean success, UpgradeType type) {
        if (!success) {
            mMainHandler.postDelayed(() -> mHmiCallback.updatePolicyManager((IHmiPolicyManager) this), 100);
            mMainHandler.postDelayed(() -> mHmiCallback.startRunPolicyError(type), 100);
        } else {
            mMainHandler.postDelayed(() -> mHmiCallback.updatePolicyManager((IHmiPolicyManager) this), 100);
            mMainHandler.postDelayed(() -> mHmiCallback.startRunPolicy(type), 100);
        }
    }

    @Override
    public void findTimeChange(long time) {
        mMainHandler.postDelayed(() -> mHmiCallback.updatePolicyManager((IHmiPolicyManager) this), 100);
        //预约时间变化
        mMainHandler.postDelayed(() -> mHmiCallback.findTimeChange(time), 100);
    }

    @Override
    public void taskStart(HmiTaskType type) {
        if (type == null) return;
        mMainHandler.postDelayed(() -> mHmiCallback.taskStart(type), 100);
    }

    @Override
    public void taskEnd(HmiTaskType type, IHmiCallback.IHmiResult result) {
        if (type == HmiTaskType.check) {
            mSession = result.isSuccess() ? CarotaClient.getClientSession() : null;
        }
        mMainHandler.postDelayed(() -> mHmiCallback.taskEnd(type, result), 100);
    }

    @Override
    public void taskRunEnd(boolean keepDownload) {
        mMainHandler.postDelayed(mHmiCallback::endRunPolicy, 100);
    }

    @Override
    public void updateInstallType() {
        mHmiDbManager.setInstallPolicyType(getUpgradeType());
    }

}
