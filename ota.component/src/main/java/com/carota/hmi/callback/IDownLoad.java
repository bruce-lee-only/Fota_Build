package com.carota.hmi.callback;

import com.carota.core.ISession;
import com.carota.hmi.action.DownLoadAction;

public abstract class IDownLoad implements ICall{

    public abstract void onDownloading(ISession s, int pro, String speed);

    public abstract void onStop(boolean success, ISession s, DownLoadAction action);

}
