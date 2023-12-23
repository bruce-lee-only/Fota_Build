package com.carota.hmi.node;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.carota.CarotaClient;
import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;
import com.carota.core.VehicleCondition;
import com.carota.hmi.EventType;
import com.carota.hmi.ICallBack;
import com.carota.hmi.IExitOtaResult;
import com.carota.hmi.IResult;
import com.carota.hmi.ISchedule;
import com.carota.hmi.ITaskVerifyResult;
import com.carota.hmi.action.DownLoadAction;
import com.carota.hmi.action.OperationAction;
import com.carota.hmi.callback.ICheck;
import com.carota.hmi.callback.IInstall;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;

public class StateMachine implements CarotaClient.IInstallViewHandlerFactory, Handler.Callback {
    private static final int TYPE_DEFULT = 0;
    private static final int TYPE_FACTORY = 1;
    private static final int TYPE_SCHEDULE = 2;
    private static final int TYPE_UPGRADE_NOW = 3;

    public static final int MESSAGE_TYPE_INIT_CHECK = 0;
    public static final int MESSAGE_TYPE_INIT_FACTORY = 1;
    public static final int MESSAGE_TYPE_INIT_CANCLE_TIME = 2;
    public static final int MESSAGE_TYPE_INIT_TIME_CHANGE = 3;
    public static final int MESSAGE_TYPE_INIT_SCHEDULE = 4;
    public static final int MESSAGE_TYPE_INIT_UPGRADE_NOW = 5;

    public static final int MESSAGE_ACTION_RUN_AGAIN = 10;
    public static final int MESSAGE_ACTION_EXIT_OTA = 11;
    public static final int MESSAGE_ACTION_NEXT_NODE = 12;

    public static final int MESSAGE_ACTION_RESET_NODE = 13;

    public static final int MESSAGE_ACTION_INSTALL_NODE = 14;

    private final SerialExecutor mExecutor;
    private final Handler mMainHandler;

    private final SharedPreferences install_type;
    private final Context mContext;
    private final ICallBack mCallBack;
    private ISession mSession;
    private final String KEY_FACTORY = "factory";
    private final String KEY_OTA = "ota";

    private INode runNode;
    private int runMode;

    private final NodeBulider[] mDefultQueue;
    private final NodeBulider[] mFactoryQueue;
    private final NodeBulider[] mSchduleQueue;
    private final NodeBulider[] mUpgradeNowQueue;

    private final List<EventType> mDefultTypeQueue;
    private final List<EventType> mFactoryTypeQueue;
    private final List<EventType> mSchduleTypeQueue;
    private final List<EventType> mUpgradeNowTypeQueue;

    public boolean factoryRun = false;

    private StateMachine(Context context, ICallBack callback, NodeBulider[] defult, NodeBulider[] factory, NodeBulider[] schdule, NodeBulider[] upgradeNow) {
        mExecutor = new SerialExecutor();
        mMainHandler = new Handler(Looper.getMainLooper(), this);
        this.mDefultQueue = defult;
        this.mFactoryQueue = factory;
        this.mSchduleQueue = schdule;
        this.mUpgradeNowQueue = upgradeNow;

        this.mDefultTypeQueue = getTypeQueue(defult);
        this.mFactoryTypeQueue = getTypeQueue(factory);
        this.mSchduleTypeQueue = getTypeQueue(schdule);
        this.mUpgradeNowTypeQueue = getTypeQueue(upgradeNow);

        mContext = context;
        mCallBack = callback;
        install_type = getContext().getSharedPreferences("carota_install_type", Context.MODE_PRIVATE);
        runMode = TYPE_DEFULT;
    }

    private List<EventType> getTypeQueue(NodeBulider[] defult) {
        List<EventType> list = new ArrayList<>();
        for (NodeBulider node : defult) {
            list.add(node.type);
        }
        return list;
    }

    public void start(long timeout) {
        mExecutor.execute(() -> {
            runNode = new InitNode(this, timeout, this);
            runNode.runNode();
        });
    }


    private void runNode(INode node) {
        if (node == null) return;
        mExecutor.execute(() -> {
            runNode = node;
            boolean isSuccess = node.runNode();
            if (node.isAutoRunNextNode() && isSuccess) {
                runNode(creatNode(getTypeInQueueIndex(node.getType()) + 1));
            }
            if (!isSuccess && inOta() && node.getType() != EventType.CONDITION) {
                runNode(creatNode(getTypeInQueueIndex(EventType.EXIT_OTA)));
            }
        });
    }

    private int getTypeInQueueIndex(EventType type) {
        switch (runMode) {
            case TYPE_FACTORY:
                return mFactoryTypeQueue.indexOf(type);
            case TYPE_SCHEDULE:
                return mSchduleTypeQueue.indexOf(type);
            case TYPE_UPGRADE_NOW:
                return mUpgradeNowTypeQueue.indexOf(type);
            default:
                return mDefultTypeQueue.indexOf(type);
        }
    }

    private NodeBulider getNodeBulider(EventType type) {
        int index = getTypeInQueueIndex(type);
        if (index != -1) {
            switch (runMode) {
                case TYPE_FACTORY:
                    return mFactoryQueue[index];
                case TYPE_SCHEDULE:
                    return mSchduleQueue[index];
                case TYPE_UPGRADE_NOW:
                    return mUpgradeNowQueue[index];
                default:
                    return mDefultQueue[index];
            }
        }
        return null;
    }

    Context getContext() {
        return mContext;
    }

    void setSession(ISession session) {
        if (mSession != session) {
            mSession = session;
        }
    }

    void saveInOta(boolean inOta) {
        install_type.edit().putBoolean(KEY_OTA, inOta).commit();
    }

    boolean inOta() {
        return install_type.getBoolean(KEY_OTA, false);
    }


    void saveInstallType() {
        install_type.edit().putInt(KEY_FACTORY, runMode).commit();
    }

    int installType() {
        return install_type.getInt(KEY_FACTORY, 0);
    }

    ISession getSession() {
        return mSession;
    }

    ICallBack getCallback() {
        return mCallBack;
    }

    public void runTaskVerifyNode(IResult callback) {
        mExecutor.execute(() -> {
            boolean success = new TaskVerifyNode((StateMachine.this)).execute();
            mMainHandler.post(() -> callback.result(success));
        });
    }

    public void setTime(long time, String tid, ISchedule callback) {
        mExecutor.execute(() -> {
            boolean success = new SetTimeNode(StateMachine.this, time, tid).execute();
            mMainHandler.post(() -> callback.result(success, time));
        });
    }

    public void vehicleCondition(ITaskVerifyResult callback) {
        mExecutor.execute(() -> {
            BaseNode node = new VehicleConditionNode(this);
            node.setExtraCondition(callback.addExtraPreCondition());
            VehicleCondition vehicleCondition = runVehicleConditionNode(node);
            mMainHandler.post(() -> callback.result(vehicleCondition, node.getVerifyResult()));
        });
    }

    //todo: add by lipiyan 2023-03-14
    public void exitOtaModel(IExitOtaResult callback) {
        mExecutor.execute(() -> {
            BaseNode node = new ExitOtaNode(this);
            node.execute();
        });
    }
    //todo: add by lipiyan 2023-03-14

    //todo: add by lipiyan 2023-06-24
    public void installUpdate(IInstall callback) {
        mExecutor.execute(() -> {
            BaseNode node = new InstallNode(this);
            node.execute();
        });
    }
    //todo: add by lipiyan 2023-06-24

    boolean isFactory() {
        return runMode == TYPE_FACTORY;
    }

    public VehicleCondition runVehicleConditionNode(BaseNode iNode) {
        BaseNode node = iNode;
        if (node == null) node = new VehicleConditionNode(this);
        node.execute();
        return node.getVehicleCondition();
    }

    private INode creatNode(int index) {
        BaseNode node = null;
        EventType type;
        boolean isAuto;
        try {
            switch (runMode) {
                case TYPE_SCHEDULE:
                    if (index >= mSchduleQueue.length) return null;
                    type = mSchduleQueue[index].type;
                    isAuto = mSchduleQueue[index].isAutoRunNext;
                    break;
                case TYPE_FACTORY:
                    if (index >= mFactoryQueue.length) return null;
                    type = mFactoryQueue[index].type;
                    isAuto = mFactoryQueue[index].isAutoRunNext;
                    break;
                case TYPE_UPGRADE_NOW:
                    if (index >= mUpgradeNowQueue.length) return null;
                    type = mUpgradeNowQueue[index].type;
                    isAuto = mUpgradeNowQueue[index].isAutoRunNext;
                    break;
                default:
                    if (index >= mDefultQueue.length) return null;
                    type = mDefultQueue[index].type;
                    isAuto = mDefultQueue[index].isAutoRunNext;
                    break;
            }
            switch (type) {
                case ENTER_OTA:
                    node = new EnterOtaNode(this);
                    break;
                case RESCUE:
                    node = new RescueNode(this);
                    break;
                case CHECK:
                    node = new CheckNode(this);
                    break;
                case DOWNLOAD:
                    node = new DownloadNode(this);
                    break;
                case CONDITION:
                    node = new VehicleConditionNode(this);
                    break;
                case INSTALL:
                    node = new InstallNode(this);
                    break;
                case TASK_VERIFY:
                    node = new TaskVerifyNode(this);
                    break;
                case EXIT_OTA:
                    node = new ExitOtaNode(this);
                    break;
            }
            if (node != null) {
                Logger.info("HMI-Node is Created @%s", node.getType());
                node.setAutoRunNextNode(isAuto);
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        return node;
    }

    @Override
    public IInstallViewHandler create(Context context) {
        if (runNode != null && runNode.getType() == EventType.INSTALL) {
            return (IInstallViewHandler) runNode;
        }
        Logger.info("HMI Resume Install");
        InstallNode node;
        switch (installType()) {
            case TYPE_UPGRADE_NOW:
                node= (InstallNode) startUpgradeNow(true, false);
                break;
            case TYPE_SCHEDULE:
                node= (InstallNode) startSchedule(true);
                break;
            case TYPE_FACTORY:
                node= (InstallNode) startFactoryTask(true);
                break;
            default:
                node= (InstallNode) startDefultTask(true);
                break;
        }
        if (node != null) {
            node.setResume();
        }
        return node;
    }

    Handler getHandler() {
        return mMainHandler;
    }

    void clearInstallType() {
        install_type.edit().putInt(KEY_FACTORY, TYPE_DEFULT).commit();
    }

    @Override
    public boolean handleMessage(android.os.Message msg) {
        switch (msg.what) {
            case MESSAGE_TYPE_INIT_CHECK:
                runMode = TYPE_DEFULT;
                //defult upgrade
                startDefultTask(false);
                break;
            case MESSAGE_TYPE_INIT_FACTORY:
                runMode = TYPE_FACTORY;
                //factory
                startFactoryTask(false);
                break;
            case MESSAGE_TYPE_INIT_CANCLE_TIME:
                //factory
                mCallBack.schedule().onScheduleCancle();
                break;
            case MESSAGE_TYPE_INIT_TIME_CHANGE:
                //Time charge
                if (((Message) msg.obj).isInit) {
                    mCallBack.down().onStop(true, getSession(), getDownLoadAction());
                }
                mCallBack.schedule().onScheduleTimeChange(((Message) msg.obj).time);
                break;
            case MESSAGE_TYPE_INIT_SCHEDULE:
                runMode = TYPE_SCHEDULE;
                //schedule
                Logger.debug("down callback before startSchedule");
                if (!((Message) msg.obj).isInit){
                    mCallBack.down().onStop(true, getSession(), getDownLoadAction());
                }
                startSchedule(false);
                break;
            case MESSAGE_TYPE_INIT_UPGRADE_NOW:
                //upgrade now
                runMode = TYPE_UPGRADE_NOW;
                if (!((Message) msg.obj).isInit)
                    mCallBack.down().onStop(true, getSession(), getDownLoadAction());
                startUpgradeNow(false, true);
                break;
            case MESSAGE_ACTION_EXIT_OTA:
                runNode(creatNode(getTypeInQueueIndex(EventType.EXIT_OTA)));
                break;
            case MESSAGE_ACTION_RUN_AGAIN:
                runNode(runNode);
                break;
            case MESSAGE_ACTION_NEXT_NODE:
                INode node = creatNode(getTypeInQueueIndex(runNode.getType()) + 1);
                if (node != null) Logger.info("HMI Run Next Action @%s", node.getType());
                runNode(node);
                break;
            case MESSAGE_ACTION_RESET_NODE:
                INode restNode = creatNode( getTypeInQueueIndex(EventType.DOWNLOAD) + 1);
                if (restNode != null) Logger.info( "HMI Run Next Action @%s", restNode.getType());
                runNode(restNode);
                break;
            case MESSAGE_ACTION_INSTALL_NODE:
                INode installNode = creatNode( getTypeInQueueIndex(EventType.INSTALL));
                if (installNode != null) Logger.info( "HMI Run Next Action @%s", installNode.getType());
                runNode(installNode);
                break;
        }
        return true;
    }

    private INode startUpgradeNow(boolean isResume, boolean isField) {
        int index = 0;
        runMode = TYPE_UPGRADE_NOW;
        if (isResume) index = getTypeInQueueIndex(EventType.INSTALL);
        INode node = creatNode(index);
        mExecutor.execute(() -> {
            if (isResume) {
                runNode(node);
            } else if (isField){
                mCallBack.canUpgradeNow();
            }else {
                Logger.warn("HMI User Not Start Upgrade Now");
            }
        });
        return node;
    }

    private INode startSchedule(boolean isResume) {
        int index = 0;
        runMode = TYPE_SCHEDULE;
        if (isResume) index = getTypeInQueueIndex(EventType.INSTALL);
        INode node = creatNode(index);
        mExecutor.execute(() -> {
            if (isResume || mCallBack.schedule().canScheduleUpgrade(new OperationAction(this))) {
                runNode(node);
            } else {
                Logger.warn("HMI User Not Start Upgrade Now");
            }
        });
        return node;
    }

    private DownLoadAction getDownLoadAction() {
        return new DownLoadAction(this, true, isAutoRunNextNode(EventType.DOWNLOAD), mMainHandler);
    }

    private boolean isAutoRunNextNode(EventType type) {
        NodeBulider bulider = getNodeBulider(type);
        if (bulider != null) return bulider.isAutoRunNext;
        return false;
    }

    private INode startFactoryTask(boolean isResume) {
        int index = 0;
        if (isResume) index = getTypeInQueueIndex(EventType.INSTALL);
        runMode = TYPE_FACTORY;
        INode node = creatNode(index);
        int finalIndex = index;
        mExecutor.execute(() -> {
            int i = finalIndex;
            BaseNode n = (BaseNode) node;
            mMainHandler.post(() -> mCallBack.factory().onFactoryStart());
            boolean success = true;
            Logger.error("HMI User Not Start Factory, User do factory by self");
//            if (isResume || mCallBack.factory().canStartFactory())
                mCallBack.factory().onFactoryStop(success, 0);
//            while (true){
//                try {
//                    if (isResume || mCallBack.factory().canStartFactory()) break;
//                    Logger.info("factory waiting for check done... ...");
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (isResume || mCallBack.factory().canStartFactory()) {
//                while (n != null) {
//                    if (!n.execute()) {
//                        success = false;
//                        break;
//                    }
//                    n = (BaseNode) creatNode(++i);
//                }
//                if (!success) {
//                    BaseNode exitOta = (BaseNode) creatNode(getTypeInQueueIndex(EventType.EXIT_OTA));
//                    if (exitOta != null) {
//                        exitOta.execute();
//                    }
//                }
//            } else {
//                Logger.error("HMI User Not Start Factory, User do factory by self");
//            }

        });
        return node;
    }

    private INode startDefultTask(boolean isResume) {
        int index = 0;
        runMode = TYPE_DEFULT;
        if (isResume) index = getTypeInQueueIndex(EventType.INSTALL);
        INode node = creatNode(index);
        runNode(node);
        return node;
    }


    public static class Bulider {
        NodeBulider[] defult;
        NodeBulider[] upgradeNow;
        NodeBulider[] factory;
        NodeBulider[] schdule;

        public Bulider addEventQueue(NodeBulider... node) {
            defult = new NodeBulider[]{new NodeBulider(EventType.CHECK, false), new NodeBulider(EventType.DOWNLOAD, false), new NodeBulider(EventType.ENTER_OTA, false), new NodeBulider(EventType.CONDITION, false), new NodeBulider(EventType.INSTALL, false), new NodeBulider(EventType.EXIT_OTA, false),};
            return this;
        }

        public Bulider addFactoryQueue(NodeBulider... node) {
            factory = new NodeBulider[]{new NodeBulider(EventType.CHECK, false), new NodeBulider(EventType.DOWNLOAD, false), new NodeBulider(EventType.ENTER_OTA, false), new NodeBulider(EventType.CONDITION, false), new NodeBulider(EventType.INSTALL, false), new NodeBulider(EventType.EXIT_OTA, false),};
            return this;
        }

        public Bulider addUpgradeNowQueue(NodeBulider... node) {
            upgradeNow = new NodeBulider[]{new NodeBulider(EventType.ENTER_OTA, false), new NodeBulider(EventType.CONDITION, false), new NodeBulider(EventType.INSTALL, false), new NodeBulider(EventType.EXIT_OTA, false)};
            return this;
        }

        public Bulider addSchduleQueue(NodeBulider... node) {
            schdule = new NodeBulider[]{new NodeBulider(EventType.DOWNLOAD, false), new NodeBulider(EventType.ENTER_OTA, false), new NodeBulider(EventType.CONDITION, false), new NodeBulider(EventType.INSTALL, false), new NodeBulider(EventType.EXIT_OTA, false)};
            return this;
        }

        public StateMachine bulid(Context context, ICallBack callback) {
            if (defult == null || factory == null || schdule == null || upgradeNow == null)
                throw new IllegalArgumentException("Hmi StateMachine Bulid Error");
            return new StateMachine(context, callback, defult, factory, schdule, upgradeNow);
        }
    }

    public static class NodeBulider {
        private final EventType type;
        private final boolean isAutoRunNext;

        public NodeBulider(EventType type, boolean isAutoRunNext) {
            this.type = type;
            this.isAutoRunNext = isAutoRunNext;
        }
    }


    public static class Message {
        public final boolean isInit;
        public final long time;

        public Message(boolean isInit) {
            this(isInit, 0);
        }

        Message(boolean isInit, long time) {
            this.isInit = isInit;
            this.time = time;
        }
    }
}
