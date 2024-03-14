package com.carota.hmi.policy;

import com.carota.hmi.policy.lock.MyLock;
import com.carota.hmi.task.TaskFactory;
import com.carota.hmi.task.callback.ITask;
import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.task.callback.ITaskDataCallback;
import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BasePolicy implements ITaskDataCallback, IPolicy {
    private final ITaskCallback mCallback;
    private final SerialExecutor mExecutor;
    private final MyLock mLock;
    private Status mRunStatus;
    private ITask mRootTask;


    protected BasePolicy(ITaskCallback callback) {
        this.mCallback = callback;
        mLock = new MyLock();
        mRunStatus = Status.idle;
        mExecutor = new SerialExecutor();
    }

    void setRootTask(ITask rootTask) {
        this.mRootTask = rootTask;
    }


    @Override
    public void run() {
        try {
            Logger.info("HMI-Task Policy Start Run Thread @%s", getUpgradeType());
            if (mRunStatus == Status.ready) {
                mRunStatus = Status.running;
            }
            if (mRootTask.run(mCallback, getUpgradeType())) {
                mRootTask.resetAllTask();
                mCallback.taskRunEnd(false);
                mRunStatus = Status.idle;
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        Logger.info("HMI-Task Policy Run Thread End @%s", getUpgradeType());
    }

    @Override
    public boolean isFactory() {
        return false;
    }

    @Override
    public final void saveInstallType() {
        mCallback.updateInstallType();
    }

    @Override
    public final boolean suspend() {
        mLock.suspend();
        return mRunStatus == Status.wait_end || mRunStatus == Status.freeze;
    }

    @Override
    public boolean remoteFreezePolicy(UpgradeType type) {
        //冻结任务
        if (mExecutor.isRunning() && !mLock.isSuspend()) {
            Logger.error("HMI-Policy Freeze Fail@%s", getUpgradeType());
            return false;
        }
        if (mLock.isSuspend()) {
            Logger.error("HMI-Policy Freeze Policy Resume  @%s", getUpgradeType());
            mRunStatus = Status.freeze;
            mLock.resume();
        }
        Logger.error("HMI-Policy Freeze Success @%s", getUpgradeType());
        return true;
    }

    @Override
    public void findTimeChange() {
        if (mRunStatus == Status.idle && mRootTask.containsTask(HmiTaskType.download)) {
            mRootTask.setSuccessIfNotSuccess(HmiTaskType.download);
            mExecutor.execute(this);
            Logger.error("HMI-Policy Reset Download Success @%s", getUpgradeType());
        }
    }

    @Override
    public boolean ready() {
        if (mRunStatus == Status.idle) {
            Logger.error("HMI-Policy Set %s to Ready @%s", mRunStatus, getUpgradeType());
            mRunStatus = Status.ready;
            return true;
        }
        if (mRunStatus == Status.freeze) {
            Logger.error("HMI-Policy Set %s to Ready @%s", mRunStatus, getUpgradeType());
            mRunStatus = Status.ready;
            mExecutor.execute(this);
            return true;
        }
        Logger.error("HMI-Policy Set %s to Ready Fail @%s", mRunStatus, getUpgradeType());
        return false;
    }

    @Override
    public boolean startPolicy() {
        if (mRunStatus == Status.ready) {
            Logger.info("HMI-User start Policy @" + getUpgradeType());
            mExecutor.execute(this);
            return true;
        }
        Logger.info("HMI-User start Policy Error @" + getUpgradeType());
        return false;
    }

    @Override
    public boolean runFailTaskAgain() {
        if (mExecutor.isRunning()) {
            Logger.info("HMI-User Run Fail Task Again Fail because Policy isRunning @" + getUpgradeType());
            return false;
        }
        if (mRootTask.havaFailTask()) {
            mExecutor.execute(this);
            Logger.info("HMI-User Start Run Fail Task Again @" + getUpgradeType());
            return true;
        }
        Logger.error(String.valueOf(mRunStatus));
        if (mRunStatus == Status.ready) {
            Logger.info("HMI-User Start Run Fail Task Again @" + getUpgradeType());
            return startPolicy();
        }
        Logger.info("HMI-User Start Run Fail Task Again Fail , Because Policy Not Find Fail Task @" + getUpgradeType());
        return false;
    }

    @Override
    public boolean runNextTaskWhenNeedUserRun() {
        if (mLock.isSuspend()) {
            Logger.info("HMI-User Start Run Next Task @" + getUpgradeType());
            mLock.resume();
            return true;
        }
        Logger.info("HMI-User Start Run Next Task Fail, Because Policy Not Need you Run @" + getUpgradeType());
        return false;
    }

    private boolean isKeepDown() {
        return getUpgradeType() == UpgradeType.DEFULT
                || getUpgradeType() == UpgradeType.SCHEDULE
                || getUpgradeType() == UpgradeType.PUSH_UPGRADE;
    }

    /**
     * 结束升级策略
     *
     * @return 0:触发失败
     * 1:暂停成功
     * 2:暂停成功，需要重新执行当前策略,用于某个节点失败后，重置策略
     */
    @Override
    public boolean endPolicy(boolean keepdown) {
        Logger.info("HMI-User Start End Policy %s@%s", keepdown ? "And Keep Download " : "", getUpgradeType());
        if (mExecutor.isRunning() && !mLock.isSuspend() || mRunStatus == Status.wait_end) {
            Logger.info("HMI-User Not End Policy When Policy is Run @%s", getUpgradeType());
            return false;
        }
        switch (mRunStatus) {
            case ready:
                mRunStatus = Status.idle;
                Logger.info("HMI-User Not End Policy When Policy is %s @%s", mRunStatus, getUpgradeType());
                return true;
            case running:
                break;
            default:
                Logger.info("HMI-User Not End Policy When Policy is %s @%s", mRunStatus, getUpgradeType());
                return false;
        }
        mRunStatus = Status.wait_end;
        EndPolicyTask endTask = new EndPolicyTask(keepdown && isKeepDown());
        mRootTask.setEndTaskData(endTask.downloadSuccess, endTask.needExitOta, endTask.taskAlive);
        if (mLock.isSuspend()) {
            mLock.resume();
        }
        mExecutor.execute(endTask);
        Logger.info("HMI-User End Policy @%s", getUpgradeType());
        return true;
    }

    @Override
    public void installStart() {
        if (mRunStatus == Status.idle) {
            mRunStatus = Status.running;
            mRootTask.setSuccessIfNotSuccess(HmiTaskType.install);
            mCallback.taskStart(HmiTaskType.install);
        }
    }

    @Override
    public void installStop() {
        mExecutor.execute(this);
    }

//    @Override
//    public void resume() {
//        //恢复冻结任务
//        if (!mExecutor.isRunning()) {
//            mRunStatus = Status.running;
//            mExecutor.execute(this);
//        }
//    }

    private enum Status {
        //空闲状态，就绪状态，运行状态，等待终止结束状态,任务冻结
        idle, ready, running, wait_end, freeze
    }

    private class EndPolicyTask implements Runnable {
        //下载成功（默认下载成功）
        private final AtomicBoolean downloadSuccess;
        //需要退Ota
        private final AtomicInteger needExitOta;
        //任务有效(默认有效)
        private final AtomicBoolean taskAlive;

        private final boolean isKeepDown;

        private EndPolicyTask(boolean isKeepDown) {
            this.isKeepDown = isKeepDown;
            downloadSuccess = new AtomicBoolean(true);
            needExitOta = new AtomicInteger(0);
            taskAlive = new AtomicBoolean(true);
        }


        @Override
        public void run() {
            Logger.info("HMI-Task Start Run End Policy Task @%s", getUpgradeType());
            if (needExitOta.get() != 0) {
                //执行退OTA
                try {
                    TaskFactory.getExitOta().run(mCallback, getUpgradeType());
                } catch (Exception e) {
                    Logger.error(e);
                }
            }
            if (!taskAlive.get()) {
                downloadSuccess.set(false);
                //任务所有状态清除
                Logger.info("HMI-P Will clear All Status in End Policy @%s", getUpgradeType());
            }
            boolean keepDownload = downloadSuccess.get() && isKeepDown;
            if (keepDownload && mRootTask.containsTask(HmiTaskType.download)) {
                Logger.info("HMI-P Will Reset Download in End Policy @%s", getUpgradeType());
                mRootTask.resetAllTask();
                mRootTask.setSuccessIfNotSuccess(HmiTaskType.download);
                mRunStatus = Status.running;
                mCallback.taskRunEnd(true);
                mExecutor.execute(BasePolicy.this);
            } else {
                mRunStatus = Status.idle;
                mRootTask.resetAllTask();
                mCallback.taskRunEnd(keepDownload);
            }
            Logger.info("HMI-Task Run End Policy Task Finish @%s", getUpgradeType());
        }
    }

}
