package com.carota.hmi.node;

import com.carota.CarotaClient;
import com.carota.CarotaVehicle;
import com.carota.core.ScheduleAttribute;
import com.carota.hmi.EventType;
import com.carota.hmi.action.InitAction;
import com.carota.rescue.RescueCarotaClient;
import com.momock.util.Logger;

/**
 * sdk init
 */
public class InitNode extends BaseNode {

    private final long timeout;
    private long mSystemClockTime;
    private long mScheduleTime = -1L;
    private final CarotaClient.IInstallViewHandlerFactory mResumeView;

    private int remoteTime = 0;

    private boolean scheduleRun = false;

    public InitNode(StateMachine status, long bootTimeout,
                    CarotaClient.IInstallViewHandlerFactory resumeView) {
        super(status);
        timeout = bootTimeout;
        mResumeView = resumeView;
    }

    @Override
    void onStart() {
        mCallBack.init().onStart();
    }

    @Override
    void onStop(boolean success) {
        mCallBack.init().onStop(success, new InitAction(mStatus, success, isAutoRunNextNode(), mHandler));
        //开启定时查询vsi是否有结果
        new Thread(() -> {
            while (true) {
                getRemoteEvent(false);
                sleep(10000);
            }
        }).start();
    }

    @Override
    public EventType getType() {
        return EventType.INIT;
    }

    @Override
    protected boolean execute() {
        //init sdk
        CarotaClient.init(mStatus.getContext(), mResumeView, timeout);
        CarotaClient.waitBootComplete("Node-Init");
        Logger.info("HMI-Node Wait Upgrade End @%s", getType());
        mStatus.setSession(CarotaClient.getClientSession());
        if (!CarotaClient.getClientStatus().isUpgradeTriggered()) {
            //init Schedule or Factory
//            MyReciver reciver = new MyReciver();
//            CarotaVehicle.registerHandler(reciver);
//            long endTime = SystemClock.elapsedRealtime() + timeout;
//            while (!reciver.init) {
//                sleep(5000);
//                if (SystemClock.elapsedRealtime() > endTime) {
//                    Logger.info("HMI-Init Schedule Fail");
//                    break;
//                }
//            }
            Logger.info("HMI-Init Start Cancle Ota");
            while (mStatus.inOta() && !CarotaVehicle.setUpgradeRuntimeEnable(false, false)) {
                sleep(10000);
            }
            mStatus.saveInOta(false);
            getRemoteEvent(true);
        }
        return true;
    }

    /**
     * @param isInit
     * @return have Remoute Event
     */
    private boolean getRemoteEvent(boolean isInit) {
        ScheduleAttribute attribute = CarotaVehicle.getSchedule();

        if (!isInit){
            if (attribute != null) {
                Logger.info("ScheduleAttribute: [scheduleType]->[%d], [scheduleTime]->[%d]", attribute.scheduleType, attribute.scheduleTime);
                switch ((int) attribute.scheduleType) {
                    case ScheduleAttribute.TYPE_FACTORY:
                        if (CarotaVehicle.setScheduleIdle()) {
                            Logger.info("HMI-Beat Start Factory @%s", getType());
                            //todo: 不管车端是否符合预约触发条件，当前有预约就拒绝执行产线任务,有预约任务的车就任务是客户手里的车
                            //todo: 该部分slave做过一次拦截，这里的处理是为了防止预约触发后，slave无法拦截的场景
                            if (scheduleRun) {
                                Logger.warn("schedule run before this, factory task return");
                                return true;
                            }
                            mStatus.factoryRun = true;
                            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_FACTORY, new StateMachine.Message(isInit)));
                        }
                        remoteTime = 10;
                        return true;
                    case ScheduleAttribute.TYPE_CANCEL:
                        if (CarotaVehicle.setScheduleIdle()) {
                            Logger.info("HMI-Beat Cancle Schedule @%s", getType());
                            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_CANCLE_TIME, new StateMachine.Message(isInit)));
                        }
                        remoteTime = 10;
                        break;
                    case ScheduleAttribute.TYPE_SILENCE:
                    case ScheduleAttribute.TYPE_NORMAL :
                        long time = attribute.scheduleTime;
                        scheduleRun = true;
                        //time 不为负
                        if (time == 0L) {
                            if (CarotaVehicle.setScheduleIdle()) {
                                Logger.info("HMI-Beat Upgrade Now @%s", getType());
                                mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_UPGRADE_NOW, new StateMachine.Message(isInit)));
                            }
                            remoteTime = 10;
                        } else if (getSystemClockTime() >= time) {
                            if (CarotaVehicle.setScheduleIdle()) {
                                //todo: mode by Lipiyan 2023-06-05 for 当前时间如果超过预约时间达到一定时长将不会触发预约升级流程，防止预约已经过了很长时间开机之后依然会有预约弹框
                                Logger.info("HMI-Beat Start Schedule Upgrade @%s", getType());
                                if (getSystemClockTime() > (time + 5 * 60 * 1000)){
                                    Logger.error("HMI-Beat Upgrade time out, schedule will be cancel!!!");
                                    mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_TIME_CHANGE, new StateMachine.Message(isInit, -1)));
                                }else {
                                    mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_SCHEDULE, new StateMachine.Message(isInit)));
                                }
                                //todo: mode by Lipiyan 2023-06-05
                            }
                            remoteTime = 10;
                        } else if (mScheduleTime != time) {
                            Logger.info("HMI-Beat Time Charge To %d @%s", time, getType());
                            mScheduleTime = time;
                            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_TIME_CHANGE, new StateMachine.Message(isInit, mScheduleTime)));
                        }
                        if (time != -1) {
                            return true;
                        }
                    case ScheduleAttribute.TYPE_IDLE:
                        if (mScheduleTime != attribute.scheduleTime){
                            mScheduleTime = attribute.scheduleTime;
                            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_TIME_CHANGE, new StateMachine.Message(isInit, mScheduleTime)));
                        }
                        break;
                }
            }

            remoteTime += 1;
            if (remoteTime == 4 && !mStatus.isFactory() && !CarotaClient.isResume() && !RescueCarotaClient.isResume()){
                if (attribute != null){
                    if (attribute.scheduleTime < 0){
                        mCallBack.init().onCanCheck(true);
                    }
                }else {
                    Logger.info("RemoteEvent run 40 second, now run check");
                    mCallBack.init().onCanCheck(true);
                }
            }
        }else {
            Logger.debug("这里是第一次进行check触发的");
//            mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_CHECK, new StateMachine.Message(true)));
            if (attribute != null && attribute.scheduleTime > 0){
                remoteTime = 10;
                mHandler.sendMessage(mHandler.obtainMessage(StateMachine.MESSAGE_TYPE_INIT_TIME_CHANGE, new StateMachine.Message(true, attribute.scheduleTime)));
            }
        }

        return false;
    }

    private long getSystemClockTime() {
        return Math.max(mSystemClockTime, System.currentTimeMillis());
    }

//    private class MyReciver extends BroadcastReceiver {
//
////        boolean init = false;
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
////            init = true;
//            if (intent != null && "BEAT".equals(intent.getStringExtra("action"))) {
//                mSystemClockTime = intent.getLongExtra("systemClock", -1);
//            } else {
//                postRunable(() -> mCallBack.schedule().onScheduleReceive(context, intent, mStatus.getSession()));
//            }
//        }
//    }
}
