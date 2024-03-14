package com.carota.hmi.policy;

import com.carota.hmi.type.UpgradeType;

public interface IPolicy extends Runnable {
    UpgradeType getUpgradeType();

    /**
     * 收到远控消息，尝试冻结策略
     *
     * @param type 远控类型
     * @return true:冻结成功
     * false:冻结失败
     */
    boolean remoteFreezePolicy(UpgradeType type);

    /**
     * 策略未处于下载时，重置到下载状态
     */
    void findTimeChange();

    /**
     * 设置策略处于就绪状态
     */
    boolean ready();

    /**
     * 策略从就绪状态切换到运行态
     *
     * @return
     */
    boolean startPolicy();

    /**
     * 策略中存在任务失败时，重新执行策略
     *
     * @return
     */
    boolean runFailTaskAgain();

    /**
     * 策略执被挂起时，策略恢复执行
     *
     * @return
     */
    boolean runNextTaskWhenNeedUserRun();

    /**
     * 终止策略执行，清除所有状态
     *
     * @return
     */
    boolean endPolicy(boolean keepdown);

    /**
     * 开始升级,重置部分任务状态
     */
    void installStart();

    /**
     * 升级结束，处理策略
     */
    void installStop();

//    void resume();
}
