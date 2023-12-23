package com.carota.hmi.callback;

import com.carota.core.ISession;
import com.carota.hmi.action.CheckAction;
import com.carota.hmi.action.DownLoadAction;

public abstract class ICheck implements ICall{

    public abstract void onStop(boolean success, ISession s, CheckAction action);

    public abstract Boolean canCheck();

    public abstract String language();
}
