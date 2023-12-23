package com.carota.rescue;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.carota.core.IDownloadCallback;
import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;
import com.carota.hmi.ICallBack;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

import java.util.concurrent.ExecutionException;

public class RescueHelper implements RescueCarotaClient.IInstallViewHandlerFactory, Handler.Callback{

    private RescueHelper instance;
    private ICallBack callBack;

    private Context context;

    private Handler mMainHandler = new Handler(Looper.getMainLooper(), this);

    private SerialExecutor mExecutor = new SerialExecutor();

    public RescueHelper getInstance(){
        if (null == instance){
            instance = new RescueHelper();
        }
        return instance;
    }

    public void SdkRescueInit(Context context, ICallBack callBack, Long timeOut){
        this.callBack = callBack;
        this.context = context;
        mExecutor.execute(() -> {
            this.callBack.init().onStart();
            RescueCarotaClient.init(context, this, timeOut);
            RescueCarotaClient.waitBootComplete("Node-Init");
            boolean isUpdate = RescueCarotaClient.getClientStatus().isUpgradeTriggered();
            if (isUpdate) {
                this.callBack.init().onError(-1);
                this.callBack.init().onStop(false, null);
            }
            else
                this.callBack.init().onStop(true, null);
        });
    }

    public void SdkRescueCheck(){
        mExecutor.execute(() -> {
            try {
                Bundle bundle = new Bundle();
                bundle.putBoolean("isFactory", false);
                ISession session;

                this.callBack.check().onStart();
                session = RescueCarotaClient.check(bundle, null);
                this.callBack.check().onStop(session != null && session.getTaskCount() > 0, session, null);
            } catch (Exception e) {
                Logger.error(e);
            }
        });
    }

    public void SdkRescueDownload(){
        mExecutor.execute(() -> {
            final boolean[] isSuccess = {false};
            final boolean[] needWait = {true};
            final ISession[] session = new ISession[1];
            try {
                callBack.down().onStart();
                boolean download = RescueCarotaClient.startDownload(new IDownloadCallback() {
                    @Override
                    public void onProcess(ISession s) {
                        mMainHandler.post(() -> callBack.down().onDownloading(s, getPro(s), getSpeed(s)));
                        session[0] = s;
                    }

                    @Override
                    public void onFinished(ISession s, boolean success) {
                        mMainHandler.post(() -> callBack.down().onDownloading(s, getPro(s), getSpeed(s)));
                        isSuccess[0] = success;
                        needWait[0] = false;
                        session[0] = s;
                    }
                });

                if (!download) {
                    isSuccess[0] = false;
                    needWait[0] = false;
                }
                while (needWait[0]) {
                    sleep(5000);
                }
                callBack.down().onStop(isSuccess[0], session[0], null);
            } catch (Exception e) {
                Logger.error(e);
            }
        });
    }

    public void SdkRescueInstall(){
        mExecutor.execute(() -> {
            try {
                boolean ret = RescueCarotaClient.install(context, false);
//                if ( !ret ) callBack.install().onError(-1);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public IInstallViewHandler create(Context context) {
        return new InstallView(context, callBack);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Logger.error(e);
        }
    }

    /**
     * 下载进度扩展方法
     */
    public static int getPro(ISession session) {
        int totalProgress = 0;
        int count = session.getTaskCount();
        if (count == 0) return 0;
        for (int i = 0; i < count; i++) {
            totalProgress += session.getTask(i).getDownloadProgress();
        }
        return totalProgress / session.getTaskCount();
    }

    /**
     * 下载速度扩展方法
     */
    @SuppressLint("DefaultLocale")
    public static String getSpeed(ISession session) {
        int count = session.getTaskCount();
        long speed = 0;
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                speed += session.getTask(i).getDownloadSpeed();
            }
        }
        long kbs = speed >> 10;

        if (kbs >= 1024) {
            return String.format("%.1f MB/S", ((float) kbs) / 1024);
        } else {
            return String.format("%d KB/S", kbs);
        }
    }
}
