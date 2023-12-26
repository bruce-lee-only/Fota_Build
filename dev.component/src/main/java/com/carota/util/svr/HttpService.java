/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util.svr;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.carota.svr.IRouterHttp;
import com.carota.svr.RouterHttp;
import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

public class HttpService extends Service {

    public final static String SVR_HOST = "test_api";

    private IRouterHttp mServer;
    private static final SerialExecutor sExecutor = new SerialExecutor();

    class HttpBinder extends Binder {

        public HttpService get() {
            return HttpService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mServer = new RouterHttp(getApplicationContext(), 0);
                mServer.setRequestHandler(null, "/debug", new DebugHandler(getApplicationContext()));
                mServer.setRequestHandler(SVR_HOST, "/debug", new DebugHandler(getApplicationContext()));
                mServer.startServer();
                Logger.info("HttpService started @ %d", mServer.getPort());
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new HttpBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int port = 0;
                if(null != mServer) {
                    port = mServer.getPort();
                    mServer.stopServer();
                }
                Logger.info("HttpService stopped @ %d", port);
            }
        });
    }

    public int ensureReady(){
        try {
            while (null == mServer || !mServer.isRunning()) {
                Thread.sleep(1000);
            }
            Logger.error("Service is Ready");
            return mServer.getPort();
        } catch (Exception e) {
            Logger.error(e);
        }
        return -1;
    }
}
