package com.carota.hmi;

import android.content.Context;

import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.dispacther.PolicyDispatcher;
import com.carota.hmi.exception.CarOtaHmiBulidException;
import com.carota.hmi.policy.IPolicyManager;
import com.carota.hmi.policy.PolicyManagerFactory;
import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;
import com.momock.util.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Hmi 使用说明<p>
 * ##Hmi初始化<p>
 * ```
 * new CarOtaHmi.CarOtaHmiPolicyBuilder(this)<br>
 * .setPolicy()<br>
 * .setPolicy()<br>
 * .build()<br>
 * .start();<br>
 * ```
 */
public class CarOtaHmi {

    private static final long TIMEOUT = 5 * 60 * 1000;

    private final IPolicyManager mManager;
    private final Context mContext;
    private final IHmiCallback mCallBack;
    private volatile PolicyDispatcher mDispatcher;

    private CarOtaHmi(Context context, IHmiCallback callBack, IPolicyManager manager) {
        mManager = manager;
        mContext = context;
        mCallBack = callBack;
    }

    public void start() {
        if (mDispatcher == null) {
            synchronized (this) {
                if (mDispatcher == null) {
                    mDispatcher = new PolicyDispatcher(mManager,mContext,mCallBack);
                    mDispatcher.start();
                }
            }
        } else {
            Logger.error("HMI-B Not Start Again");
        }
    }

    /**
     * Hmi 升级策略定义
     */
    public static class CarOtaHmiPolicyBuilder {
        //结果回调接口
        private final IHmiCallback mCallBack;
        private final Context mContext;

        private final Map<UpgradeType, LinkedList<HmiTaskType>> mPolicyMap;
        private final long mTimeOut;

        public CarOtaHmiPolicyBuilder(Context context, IHmiCallback callback) {
            this(context, 0, callback);
        }

        public CarOtaHmiPolicyBuilder(Context context, long timout, IHmiCallback callback) {
            if (null == context) throw new CarOtaHmiBulidException("Context is Null");
            if (null == callback) throw new CarOtaHmiBulidException("IHmiCallback is Null");
            this.mTimeOut = timout > 0 ? timout : TIMEOUT;
            this.mCallBack = callback;
            this.mContext = context.getApplicationContext();
            this.mPolicyMap = new HashMap<>();
        }

        /**
         * 设置升级策略
         * <p>
         * 1.默认策略不允许为空
         * 2.预约升级策略,没有使用默认策略download之后的操作
         * 3.推送立即升级流程,没有使用默认升级download之后的操作
         * 4.工厂策略未设置使用如下策略:
         * ```
         * List<HmiTaskType> list = new LinkedList<>();
         * list.add(HmiTaskType.enter_ota);
         * list.add(HmiTaskType.check);
         * list.add(HmiTaskType.download);
         * list.add(HmiTaskType.condition);
         * list.add(HmiTaskType.task_timeout_verify);
         * list.add(HmiTaskType.install);
         * list.add(HmiTaskType.exit_ota);
         * Policy().addHmiTask(list)
         * ```
         *
         * @param type   升级类型
         * @param policy 默认升级流程
         * @return CarOtaHmiPolicyBuilder
         */
        public CarOtaHmiPolicyBuilder setPolicy(UpgradeType type, Policy policy) {
            if (type == null) throw new NullPointerException("Hmi UpgradeType is null");
            if (mPolicyMap.get(type) != null) {
                throw new CarOtaHmiBulidException("Hmi Policy is repeat@" + type);
            }
            mPolicyMap.put(type, policy.task);
            return this;
        }

        public CarOtaHmi build() {
            LinkedList<HmiTaskType> defult = mPolicyMap.get(UpgradeType.DEFULT);
            if (defult == null) {
                throw new CarOtaHmiBulidException("Defult Policy is Null");
            } else if (defult.contains(HmiTaskType.enter_ota)
                    && defult.indexOf(HmiTaskType.download) > defult.indexOf(HmiTaskType.enter_ota)) {
                //默认流程 进ota必须在下载完成之后
                throw new CarOtaHmiBulidException("`enter_ota` must after `download` in the Policy@DEFULT");
            }
            mPolicyMap.forEach((upgradeType, task) -> checkUndefultPolicyTask(task, upgradeType));
            IPolicyManager manager = new PolicyManagerFactory(mContext, mTimeOut, mPolicyMap);
            return new CarOtaHmi(mContext,mCallBack,manager);
        }

        private void checkUndefultPolicyTask(LinkedList<HmiTaskType> task, UpgradeType type) {
            if (task == null) return;
            Logger.info("The Policy Start verify Sort" + type);
            checkTask(type, task);
            switch (type) {
                case PUSH_UPGRADE:
                case SCHEDULE:
                    if (task.contains(HmiTaskType.check)) {
                        throw new CarOtaHmiBulidException("The Policy Not Allow Set `check`", type);
                    }
                    if (task.contains(HmiTaskType.download)) {
                        throw new CarOtaHmiBulidException("The Policy Not Allow Set `download`", type);
                    }
                    break;
                default:
                    if (!task.contains(HmiTaskType.check)) {
                        throw new CarOtaHmiBulidException("The Defult Policy Not Find `check`", type);
                    }
                    if (!task.contains(HmiTaskType.download)) {
                        throw new CarOtaHmiBulidException("The Policy Not Find `download`", type);
                    }
                    break;
            }
            Logger.info("The Policy verify Sort Success" + type);
        }

        private void checkTask(UpgradeType type, LinkedList<HmiTaskType> task) {
            //必须包含install
            if (!task.contains(HmiTaskType.install))
                throw new CarOtaHmiBulidException("`install` Not Find in the Policy", type);
            //处理wait
            checkWaitTask(type, task);
            //处理重复,校验进退ota
            checkRepeatTask(type, task);
            //验证任务执行顺序
            int check = task.indexOf(HmiTaskType.check);
            int down = task.indexOf(HmiTaskType.download);

            int install = task.indexOf(HmiTaskType.install);
            int taskTimeOut = task.indexOf(HmiTaskType.task_timeout_verify);
            if (check != -1 || down != -1) {
                //检测必须在下载之前
                if (check > down)
                    throw new CarOtaHmiBulidException("`download` must be after `check` in the Policy", type);
                //升级必须在下载之后之后
                if (install > -1 && down > install)
                    throw new CarOtaHmiBulidException("`install` must be after `download` in the Policy", type);
                //任务超时必须在下载之后

                if (taskTimeOut > -1 && down > taskTimeOut)
                    throw new CarOtaHmiBulidException("`task_timeout_verify` must be after `download` in the Policy", type);
            }

            //任务超时顺序校验
            if (taskTimeOut > -1 && install < taskTimeOut) {
                throw new CarOtaHmiBulidException("`install` must be after `task_timeout_verify` in the Policy", type);
            }
        }

        /**
         * 校验重复task
         * check,download,enterota,exitota,install,taskVerify 只能设置一次
         *
         * @param type
         * @param task
         */
        private void checkRepeatTask(UpgradeType type, LinkedList<HmiTaskType> task) {
            int check = 0;
            int download = 0;
            int enter_ota = 0;
            int exit_ota = 0;
            int install = 0;
            int task_timeout_verify = 0;
            for (HmiTaskType taskType : task) {
                switch (taskType) {
                    case check:
                        check++;
                        break;
                    case download:
                        download++;
                        break;
                    case enter_ota:
                        enter_ota++;
                        break;
                    case task_timeout_verify:
                        task_timeout_verify++;
                        break;
                    case install:
                        install++;
                        if (enter_ota > -1) {
                            //如项目需要进退ota，升级必须在进ota之前,退ota之后
                            if (enter_ota <= exit_ota) {
                                throw new CarOtaHmiBulidException("`install` must be between `enter_ota` and `exit_ota` in the Policy", type);
                            }
                        }
                        break;
                    case exit_ota:
                        exit_ota++;
                        break;
                }
                if (check > 1 || download > 1 || install > 1 || task_timeout_verify > 1)
                    throw new CarOtaHmiBulidException(String.format("`%s` is Repeat in the Policy", taskType), type);
                if (exit_ota > enter_ota) {
                    throw new CarOtaHmiBulidException("`enter_ota` must be before `exit_ota` in the Policy");
                }
                if (enter_ota > exit_ota + 1) {
                    throw new CarOtaHmiBulidException("`exit_ota` must be between two `enter_ota` in the Policy");
                }
            }

            if (enter_ota != exit_ota) {
                throw new CarOtaHmiBulidException("The quantity of `enter_ota` and `exit_ota` is different in the Policy", type);
            }

        }

        /**
         * 等待用task设置在策略第一位or最后一位，且不能在相邻位置出现两个
         *
         * @param type 升级类型
         * @param task
         */
        private void checkWaitTask(UpgradeType type, LinkedList<HmiTaskType> task) {
            if (task.getFirst() == HmiTaskType.wait_user_run_next) {
                throw new CarOtaHmiBulidException("`wait_user_run_next` is first in the Policy", type);
            }
            if (task.getLast() == HmiTaskType.wait_user_run_next) {
                throw new CarOtaHmiBulidException("`wait_user_run_next` is Last in the Policy", type);
            }
            boolean isEmpty = false;
            for (HmiTaskType s : task) {
                if (s == HmiTaskType.wait_user_run_next) {
                    if (isEmpty)
                        throw new CarOtaHmiBulidException("`wait_user_run_next` is Repeat in the Policy", type);
                    isEmpty = true;
                } else {
                    isEmpty = false;
                }
            }
        }
    }


    /**
     * 升级策略流程自定义
     */
    public static class Policy {
        private final LinkedList<HmiTaskType> task;

        public Policy() {
            this.task = new LinkedList<>();
        }

        public Policy addHmiTask(HmiTaskType... task) {
            List<HmiTaskType> types = Arrays.asList(task);
            if (types.contains(null)) throw new CarOtaHmiBulidException("HmiTaskType... Have Null");
            this.task.addAll(types);
            return this;
        }

        public Policy addHmiTask(HmiTaskType task) {
            if (task == null) throw new CarOtaHmiBulidException("HmiTaskType is Null");
            this.task.addLast(task);
            return this;
        }
    }


}
