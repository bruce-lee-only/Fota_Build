package com.carota.hmi;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.carota.CarotaClient;
import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;
import com.carota.hmi.action.FactoryAction;
import com.carota.hmi.action.ScheduleAction;
import com.carota.hmi.action.UpgradeNowAction;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.callback.ICall;
import com.carota.hmi.callback.ICallBack;
import com.carota.hmi.callback.IInstallCall;
import com.carota.hmi.node.INode;
import com.carota.hmi.node.NodeFactory;
import com.carota.hmi.node.holder.IHolder;
import com.carota.hmi.status.HmiStatus;
import com.carota.hmi.remote.RemoteMessage;
import com.carota.hmi.status.UpgradeStaus;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

public class StateMachine implements Handler.Callback, IInstallViewHandler {


    //    public static final int MESSAGE_TYPE_CHECK = 0;
    public static final int MESSAGE_TYPE_FACTORY = 1;
    public static final int MESSAGE_TYPE_CANCLE_TIME = 2;
    public static final int MESSAGE_TYPE_TIME_CHANGE = 3;
    public static final int MESSAGE_TYPE_SCHEDULE = 4;
    public static final int MESSAGE_TYPE_UPGRADE_NOW = 5;

    private final SerialExecutor mExecutor;
    private final Handler mMainHandler;


    private final Context mContext;
    private final HmiStatus mStatus;
    private final NodeFactory mNodeFactory;
    private final CallBackManager mCallManager;


    public StateMachine(Context context, ICallBack callback, long bootTimeout) {
        mExecutor = new SerialExecutor();
        mMainHandler = new Handler(Looper.getMainLooper(), this);
        mContext = context;
        mCallManager = new CallBackManager(callback);
        mStatus = new HmiStatus(context);
        mNodeFactory = new NodeFactory(mStatus, mMainHandler, mContext, mCallManager);
        initSdk(bootTimeout);
    }

    private void initSdk(long bootTimeout) {
        mExecutor.execute(() -> {
            mCallManager.onInitStart();
            Logger.info("Hmi Start Init");
            try {
                //init sdk
                CarotaClient.init(mContext, context -> StateMachine.this, bootTimeout);
                mStatus.setSession(CarotaClient.getClientSession());
                // TODO: 2023/7/3 这里是否等消息待定
                new RemoteMessage().start(mMainHandler);
                if (!CarotaClient.getClientStatus().isUpgradeTriggered()) {
                    while (mStatus.inOta()) {
                        Logger.info("Hmi Exit Ota Mode when Init");
                        mNodeFactory.getNode(EventType.EXIT_OTA).call();
                    }
                }

            } catch (Exception e) {
                Logger.error(e);
            }
            Logger.info("Hmi Init End");
            mCallManager.onInitEnd();
        });
    }

    private void runHolder(IHolder holder, ICallBack.IRemote call) {
        mExecutor.execute(() -> {
            mMainHandler.post(call::onStart);
            mStatus.setAutoRuning(true);
            if (!holder.call()) {
                while (!mNodeFactory.getNode(EventType.EXIT_OTA).call()) {
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        Logger.error(e);
                    }
                }
                mStatus.setAutoRuning(false);
                mMainHandler.post(() -> call.onStop(false, mStatus));
            }
        });
    }

    private ICallBack.IRemote getIRemoteCall(UpgradeType type) {
        switch (type) {
            case FACTORY:
                return mCallManager.factory();
            case UPGRADE_NOW:
                return mCallManager.upgradeNow();
            case SCHEDULE:
                return mCallManager.schedule();
        }
        return null;
    }


    @Override
    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case MESSAGE_TYPE_FACTORY:
                mStatus.setUpgradeType(UpgradeType.FACTORY);
                //factory
                mCallManager.factory().onFactory(new FactoryAction(this));
                break;
            case MESSAGE_TYPE_CANCLE_TIME:
                //Time Cancle
                mStatus.setUpgradeType(UpgradeType.DEFULT);
                mCallManager.schedule().onScheduleTimeCancle();
                break;
            case MESSAGE_TYPE_TIME_CHANGE:
                mStatus.setUpgradeType(UpgradeType.SCHEDULE);
                //Time charge
                mCallManager.schedule().onScheduleTimeChange((Long) msg.obj);
                break;
            case MESSAGE_TYPE_SCHEDULE:
                mStatus.setUpgradeType(UpgradeType.SCHEDULE);
                //schedule
                mCallManager.schedule().onScheduleUpgrade(new ScheduleAction(this));
                break;
            case MESSAGE_TYPE_UPGRADE_NOW:
                //upgrade now
                mStatus.setUpgradeType(UpgradeType.UPGRADE_NOW);
                mCallManager.upgradeNow().onUpgradeNow(new UpgradeNowAction(this));
                break;
        }
        return true;
    }

    @Override
    public boolean onInstallStart(ISession s) {
        mStatus.setSession(s);
        if (!mStatus.isFactory() && mStatus.getUpgradeStatus(EventType.INSTALL) == UpgradeStaus.IDIL)
            mMainHandler.post(() -> mCallManager.getICall(EventType.INSTALL).onStart(mStatus.getUpgradeType()));
        mStatus.setUpgradeStatus(EventType.INSTALL, UpgradeStaus.RUNNING);
        return true;
    }

    @Override
    public void onInstallProgressChanged(ISession s, int state, int successCount) {
        mStatus.setSession(s);
        mStatus.setInstallState(state);
        mStatus.setInstallSuccessCount(successCount);
        if (!mStatus.isFactory())
            mMainHandler.post(() -> ((IInstallCall) mCallManager.getICall(EventType.INSTALL)).onInstallProgressChanged(mStatus.getUpgradeType(), mStatus));
    }

    @Override
    public boolean onInstallStop(ISession s, int state) {
        mStatus.setSession(s);
        mStatus.setInstallState(state);
        mStatus.setUpgradeStatus(EventType.INSTALL, state == 2 ? UpgradeStaus.SUCCESS : UpgradeStaus.FAIL);
        if (!mStatus.isFactory()) {
            Boolean exitOta = mNodeFactory.getNode(EventType.EXIT_OTA).call();
            mCallManager.factory().onStop(true, mStatus);
            mStatus.setUpgradeType(UpgradeType.DEFULT);
        } else {
            mCallManager.getICall(EventType.INSTALL).onEnd(mStatus.getUpgradeType(), true, mStatus);
            mCallManager.removeCallBack(EventType.INSTALL);
        }
        return false;
    }

    public void start(UpgradeType type) {
        ICallBack.IRemote call = getIRemoteCall(type);
        if (call == null) {
            Logger.info("Hmi Not Auto Run @%s", type);
            return;
        }
        if (mExecutor.isRunning()) {
            Logger.error("Hmi Not run Holder,beacuse Node isRun @%s", type);
            call.onError(ErrorCode.ERROR_NODE_RUNNING);
            return;
        }
        if (type != mStatus.getUpgradeType()) {
            Logger.info("Hmi Not Auto Run ,beacuse Upgrade Type is Different @%s", type);
            call.onError(ErrorCode.ERROR_UPGRADE_TYPE_DIFFERENT);
            return;
        }
        Logger.info("Hmi Start Auto Run @%s", type);
        switch (type) {
            case FACTORY:
                //start Factory
                runHolder(mNodeFactory.getFactory(), call);
                break;
            case SCHEDULE:
                //start schedule
                runHolder(mNodeFactory.getSchedule(), call);
                break;
            case UPGRADE_NOW:
                //start upgrade_now
                runHolder(mNodeFactory.getUpgradeNow(), call);
                break;
            default:
                call.onError(ErrorCode.ERROR_NOT_FIND_REMOTE);
                break;
        }
    }

    public void cancle(UpgradeType type) {
        ICallBack.IRemote call = getIRemoteCall(type);
        if (call == null) {
            Logger.info("Hmi Auto Run Not Cancle ,beacuse Type Error @%s", type);
            return;
        }
        if (mExecutor.isRunning()) {
            Logger.error("Hmi Auto Run Not Cancle , beacuse Hmi isRun");
            call.onError(ErrorCode.ERROR_NODE_RUNNING);
            return;
        }
        if (type != mStatus.getUpgradeType()) {
            Logger.info("Hmi Auto Run Not Cancle,beacuse Upgrade Type is Different@%s", type);
            call.onError(ErrorCode.ERROR_UPGRADE_TYPE_DIFFERENT);
            return;
        }
        Logger.info("Hmi Cancle Auto Run @%s", type);
        switch (type) {
            case FACTORY:
                mCallManager.factory().onCancle();
                mStatus.setUpgradeType(UpgradeType.DEFULT);
                break;
            case SCHEDULE:
                mCallManager.schedule().onCancle();
                mStatus.setUpgradeType(UpgradeType.DEFULT);
                break;
            case UPGRADE_NOW:
                mCallManager.upgradeNow().onCancle();
                mStatus.setUpgradeType(UpgradeType.DEFULT);
            default:
                call.onError(ErrorCode.ERROR_NOT_FIND_REMOTE);
                break;
        }
    }

    public void runNode(UpgradeType type, EventType node, ICall call) {
        runNode(mNodeFactory.getNode(node), type, node, call);
    }

    private boolean canRunNode(UpgradeType type, EventType node, ICall call) {

        return false;
    }


    public void runTaskVerifyNode(UpgradeType type, ICall call) {
        runNode(mNodeFactory.getNode(EventType.TASK_VERIFY), type, EventType.TASK_VERIFY, call);
    }

    public void setTime(UpgradeType type, long time, ICall call) {
        runNode(mNodeFactory.getSetTime(time), type, EventType.SET_TIME, call);
    }

    public void vehicleCondition(UpgradeType type, ICall call) {
        runNode(mNodeFactory.getNode(EventType.CONDITION), type, EventType.CONDITION, call);
    }

    private void runNode(INode node, UpgradeType type, EventType eventType, ICall call) {
        mCallManager.setCallBack(eventType, call);
        if (type == UpgradeType.FACTORY) {
            Logger.error("Hmi Node not run ,beacuse is Factory");
            call.onError(type, ErrorCode.ERROR_FACTORY_NODE_NOT_RUN);
        } else if (node == null) {
            Logger.error("Hmi Node not run ,beacuse Node is Null");
            if (call != null) call.onError(type, ErrorCode.ERROR_NODE_IS_NULL);
        } else if (node.getType() == null) {
            Logger.error("Hmi Node not run ,beacuse Node Type is Null");
            call.onError(type, ErrorCode.ERROR_NODE_TYPE_IS_NULL);
        } else if (mExecutor.isRunning()) {
            Logger.error("Hmi Node not run ,beacuse Hava Node Running");
            mCallManager.getICall(eventType).onError(type, ErrorCode.ERROR_NODE_RUNNING);
        } else if (type != mStatus.getUpgradeType()) {
            Logger.error("Hmi Node not run ,beacuse Upgrade Type is Different");
            mCallManager.getICall(eventType).onError(type, ErrorCode.ERROR_UPGRADE_TYPE_DIFFERENT);
        } else if (mStatus.isAutoRuning()) {
            Logger.error("Hmi Node not run ,beacuse Node AutoRuning");
            mCallManager.getICall(eventType).onError(type, ErrorCode.ERROR_NODE_AUTO_RUNNING);
        } else if (disableRunNode(node)) {
            mCallManager.getICall(eventType).onError(type, ErrorCode.ERROR_NODE_DIEABLE_RUN);
        } else {
            mExecutor.execute(node::call);
            return;
        }
        mCallManager.removeCallBack(eventType);
    }

    private boolean disableRunNode(INode node) {
        EventType type = node.getType();
        if (mStatus.getUpgradeStatus(EventType.INSTALL) == UpgradeStaus.RUNNING) {
            Logger.error("Hmi Node not run ,beacuse Install is Running @%s", type);
        } else if (mStatus.inOta() && (type == EventType.CHECK || type == EventType.DOWNLOAD
                || type == EventType.SET_TIME || type == EventType.ENTER_OTA)) {
            Logger.error("Hmi Node not run ,beacuse In Ota @%s", type);
        } else if (type == EventType.DOWNLOAD && !mStatus.canDownload()) {
            Logger.error("Hmi Node not run ,beacuse Not Find Task @%s", type);
        } else if (!mStatus.downloadResult() && type != EventType.DOWNLOAD && type != EventType.CHECK && type != EventType.EXIT_OTA) {
            Logger.error("Hmi Node not run ,beacuse Not Download Success @%s", type);
        } else {
            return false;
        }
        return true;
    }

}
