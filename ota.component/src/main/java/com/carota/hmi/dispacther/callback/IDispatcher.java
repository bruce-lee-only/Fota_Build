package com.carota.hmi.dispacther.callback;

import android.content.Context;

import com.carota.core.ISession;
import com.carota.hmi.callback.IHmiCallback;
import com.carota.hmi.policy.BasePolicy;
import com.carota.hmi.task.callback.ITaskCallback;
import com.carota.hmi.type.UpgradeType;

public interface IDispatcher {

    IHmiCallback getHmiCallback();

    BasePolicy getPolicyTask(UpgradeType type, ITaskCallback callback);

    boolean runTask(UpgradeType type, Runnable runnable);

    boolean isRunning();

    ISession getSession();

    void endPolicy(UpgradeType type, boolean keepDown);

    Context getContext();

    void saveInstallType(UpgradeType type);

    void updateSession(ISession session);

    void stop();
}
