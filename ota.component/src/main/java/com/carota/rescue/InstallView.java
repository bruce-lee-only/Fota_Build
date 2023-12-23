package com.carota.rescue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;
import com.carota.hmi.ICallBack;
import com.momock.util.Logger;

import okhttp3.internal.http2.Http2Reader;

public class InstallView implements IInstallViewHandler, Handler.Callback {

    private Context context;
    private ICallBack callBack;

    private Handler mMainHandler = new Handler(Looper.getMainLooper(), this);

    InstallView(Context context, ICallBack callBack){
        this.context    = context;
        this.callBack   = callBack;
    }

    @Override
    public boolean onInstallStart(ISession s) {
        Logger.info("Rescue update install started");
        mMainHandler.post(() -> callBack.install().onStart(s));
        return true;
    }

    @Override
    public void onInstallProgressChanged(ISession s, int state, int successCount) {
        Logger.info("Rescue update install Progress Changed!!");
        mMainHandler.post(() -> callBack.install().onInstallProgressChanged(s, state, successCount));
    }

    @Override
    public boolean onInstallStop(ISession s, int state) {
        Logger.info("Rescue update install stop：" + state);
        mMainHandler.post(() -> callBack.install().onStop(true, null, s, state));
        return false;
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }
}
