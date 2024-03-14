package com.carota.hmi.policy;

import android.content.Context;

import com.carota.hmi.task.TaskFactory;
import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.task.callback.InitTaskCallback;
import com.carota.hmi.type.HmiTaskType;
import com.carota.hmi.type.UpgradeType;

import java.util.LinkedList;
import java.util.Map;

public class PolicyManagerFactory implements IPolicyManager {

    private final Map<UpgradeType, LinkedList<HmiTaskType>> mMap;

    private final long mTimeOut;
    private final Context mContext;

    public PolicyManagerFactory(Context c, long t, Map<UpgradeType, LinkedList<HmiTaskType>> map) {
        mMap = map;
        mTimeOut = t;
        this.mContext = c;
    }

    @Override
    public Runnable getInitPolicy(InitTaskCallback callback) {
        return TaskFactory.getInit(mContext
                , mTimeOut
                , mMap.containsKey(UpgradeType.SCHEDULE)
                        || mMap.containsKey(UpgradeType.PUSH_UPGRADE)
                        || mMap.containsKey(UpgradeType.FACTORY)
                , callback);
    }

    @Override

    public IPolicy getNewPolicy(UpgradeType type, ITaskCallback callback) {
        if (mMap.containsKey(type)) {
            return getPolicyManager(type, callback);
        }
        return null;
    }

    private IPolicy getPolicyManager(UpgradeType type, ITaskCallback callback) {
        BasePolicy policy;
        switch (type) {
            case DEFULT:
                policy = new DefultPolicy(callback);
                break;
            case FACTORY:
                policy = new FactoryPolicy(callback);
                break;
            case SCHEDULE:
                policy = new SchedulePolicy(callback);
                break;
            case PUSH_UPGRADE:
                policy = new PushUpgradePolicy(callback);
                break;
            default:
                policy = new BasePolicy(callback) {
                    @Override
                    public UpgradeType getUpgradeType() {
                        return type;
                    }
                };
                break;
        }
        policy.setRootTask(TaskFactory.getRootTask(mMap.get(type), mContext,policy));
        return policy;
    }
}
