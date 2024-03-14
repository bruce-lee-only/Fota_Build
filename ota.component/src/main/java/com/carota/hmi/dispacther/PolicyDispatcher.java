package com.carota.hmi.dispacther;

import android.content.Context;

import com.carota.CarotaVehicle;
import com.carota.core.ISession;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.policy.IPolicy;
import com.carota.hmi.policy.IPolicyManager;
import com.carota.hmi.type.UpgradeType;
import com.momock.util.Logger;

import java.util.LinkedList;

public final class PolicyDispatcher extends UserManageDispacher {
    private final IPolicyManager mPolicyFactory;
    private final LinkedList<IPolicy> mPolicyQueue;

    public PolicyDispatcher(IPolicyManager factory, Context context, IHmiCallback callback) {
        super(context, callback);
        this.mPolicyFactory = factory;
        mPolicyQueue = new LinkedList<>();
        mPolicyQueue.addFirst(mPolicyFactory.getNewPolicy(UpgradeType.DEFULT, this));
    }

    public void start() {
        //启动初始化流程
        mThreadPool.execute(mPolicyFactory.getInitPolicy(this));
    }

    @Override
    public UpgradeType getUpgradeType() {
        return mPolicyQueue.getLast().getUpgradeType();
    }

    @Override
    public void findTimeChange(long time) {
        super.findTimeChange(time);
        mPolicyQueue.getFirst().findTimeChange();
    }

    @Override
    public void initEnd(boolean cancheck) {
        super.initEnd(cancheck);
        if (cancheck) {
            mPolicyQueue.getFirst().ready();
        }
    }

    @Override
    public void findRemoteUprade(UpgradeType type) {
        IPolicy policy = mPolicyQueue.getLast();
        IPolicy iPolicy = mPolicyFactory.getNewPolicy(type, this);
        if (iPolicy == null || policy.getUpgradeType() == type
                || !policy.remoteFreezePolicy(type) || !iPolicy.ready()) {
            /*
             * -1 – 无效值
             * 0 – 没有错误
             * 1 – 不满足唤醒条件
             * 2 – 唤醒主控失败
             * 3 – 唤醒整车失败
             * 4 – 不满足升级车况
             */
            Logger.info("HMI-D Start %s Policy Error", type);
            CarotaVehicle.setErrorWakeUp(4, "不满足升级车况");
            if (policy.getUpgradeType() != type) sendStartPolicy(false, type);
        } else {
            Logger.info("HMI-D Start %s Policy", type);
            mPolicyQueue.addLast(iPolicy);
            sendStartPolicy(true, type);
        }
    }


    @Override
    public boolean startPolicy() {
        return mPolicyQueue.getLast().startPolicy();
    }

    @Override
    public boolean runFailTaskAgain() {
        return mPolicyQueue.getLast().runFailTaskAgain();
    }

    @Override
    public boolean runNextTaskWhenNeedUserRun() {
        return mPolicyQueue.getLast().runNextTaskWhenNeedUserRun();
    }

    @Override
    public boolean endPolicy() {
        return mPolicyQueue.getLast().endPolicy(false);
    }

    @Override
    public boolean endPolicyKeepDownload() {
        return mPolicyQueue.getLast().endPolicy(true);
    }

    @Override
    boolean installStart(UpgradeType type) {
        IPolicy policy = type == UpgradeType.DEFULT ? mPolicyQueue.getFirst() : mPolicyQueue.getLast();
        if (policy.getUpgradeType() != type) {
            policy = mPolicyFactory.getNewPolicy(type, this);
            mPolicyQueue.addLast(policy);
        }
        policy.installStart();
        return true;
    }

    @Override
    public boolean onInstallStop(ISession s, int state) {
        Logger.info("HMI-D IView Stop");
        mPolicyQueue.getLast().installStop();
        return super.onInstallStop(s, state);
    }

    @Override
    public void taskRunEnd(boolean keepDownload) {
        super.taskRunEnd(keepDownload);
        if (keepDownload) {
            IPolicy policy = mPolicyQueue.getFirst();
            mPolicyQueue.clear();
            mPolicyQueue.addFirst(policy);
        } else {
            mPolicyQueue.clear();
            mPolicyQueue.addFirst(mPolicyFactory.getNewPolicy(UpgradeType.DEFULT, this));
        }
        mPolicyQueue.getFirst().ready();
    }
}
