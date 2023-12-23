package com.carota.hmi.callback;

import com.carota.core.ISession;
import com.carota.hmi.action.InstallAction;

public abstract class IInstall implements ICall {

    @Override
    public final void onStart() {
        throw new RuntimeException("Not Use");
    }

    public abstract void onStart(ISession s);

    public abstract void onInstallProgressChanged(ISession s, int state, int successCount);

    public abstract void onStop(boolean succsss, InstallAction action, ISession s, int state);
}
