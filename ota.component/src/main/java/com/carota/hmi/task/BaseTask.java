package com.carota.hmi.task;

import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.exception.HmiInsitallEndExecption;
import com.carota.hmi.exception.HmiInterruptedException;
import com.carota.hmi.task.callback.ITask;
import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.task.status.TaskStatus;
import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;
import com.momock.util.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseTask implements ITask {

    private BaseTask mNext;

    private TaskStatus status;

    BaseTask() {
        status = TaskStatus.idel;
    }


    void addNext(BaseTask hmiTask) {
        if (mNext != null) {
            mNext.addNext(hmiTask);
        } else {
            mNext = hmiTask;
        }
    }

    @Override
    public final boolean run(ITaskCallback hmiCallback, UpgradeType upgradeType) throws Exception {
        try {
            if (status == TaskStatus.idel || status == TaskStatus.fail) {
                Logger.info("HMI-Task Start Run `%s` @%s", getType(), upgradeType);
                if (getType() != HmiTaskType.wait_user_run_next) {
                    hmiCallback.taskStart(getType());
                }
                status = TaskStatus.running;
                IHmiCallback.IHmiResult result = runNode();
                status = result.isSuccess() ? TaskStatus.success : TaskStatus.fail;
                Logger.info("HMI-Task `%s` Run Result:%s @%s", getType(), status, upgradeType);
                if (getType() != HmiTaskType.wait_user_run_next) {
                    if (getType() == HmiTaskType.install) {
                        if (!result.isSuccess()) {
                            hmiCallback.taskEnd(getType(), result);
                        } else {
                            throw new HmiInsitallEndExecption();
                        }
                    } else {
                        hmiCallback.taskEnd(getType(), result);
                    }
                }
            }
            if (status == TaskStatus.success) {//当前节点执行成功，运行下一节点
                if (mNext == null) {
                    return true;
                } else {
                    return mNext.run(hmiCallback, upgradeType);
                }
            }
        } catch (HmiInterruptedException e) {
            status = TaskStatus.idel;
            Logger.error(e.getMessage() + " @" + upgradeType);
        }
        return false;
    }

    @Override
    public void resetAllTask() {
        status = TaskStatus.idel;
        if (mNext != null) mNext.resetAllTask();
    }

    @Override
    public void setEndTaskData(AtomicBoolean download, AtomicInteger needExitOta, AtomicBoolean taskAlive) {
        switch (getType()) {
            case download:
                download.set(status == TaskStatus.success);
                break;
            case enter_ota:
                if (status == TaskStatus.success) needExitOta.getAndIncrement();
                break;
            case exit_ota:
                if (status == TaskStatus.success) needExitOta.getAndDecrement();
                break;
            case task_timeout_verify:
                if (status == TaskStatus.fail && taskAlive.get()) taskAlive.set(false);
                break;
            case install:
                if (status != TaskStatus.idel && taskAlive.get()) taskAlive.set(false);
                break;

        }
        if (mNext != null) mNext.setEndTaskData(download, needExitOta, taskAlive);
    }

    /**
     * run node task
     *
     * @return TaskStatus.success or TaskStatus.fail
     */
    abstract IHmiCallback.IHmiResult runNode() throws HmiInterruptedException;

    abstract HmiTaskType getType();

    public void clearStatus() {
        status = TaskStatus.idel;
    }


    private void setSuccess(HmiTaskType toTaskType) {
        if (status == TaskStatus.idel) status = TaskStatus.success;
        if (toTaskType == getType()) return;
        if (mNext != null) mNext.setSuccess(toTaskType);
    }


    @Override
    public final void setSuccessIfNotSuccess(HmiTaskType toTaskType) {
        if (!containsTask(toTaskType)) return;
        setSuccess(toTaskType);
    }


    @Override
    public final boolean containsTask(HmiTaskType type) {
        return getType() == type || (mNext != null && mNext.containsTask(type));
    }

    @Override
    public final boolean havaFailTask() {
        if (status == TaskStatus.fail) return true;
        return mNext != null && mNext.havaFailTask();
    }

}
