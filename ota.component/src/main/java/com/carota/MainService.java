/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;

import com.carota.build.ParamRAS;
import com.carota.mda.deploy.IDeploySafety;
import com.carota.mda.security.ISecuritySolution;
import com.carota.sda.ISlaveDownloadAgent;
import com.carota.sda.ISlaveMethod;
import com.carota.sda.UpdateSlave;
import com.carota.sda.provider.SlaveDownloadAgentService;
import com.carota.sda.util.SlaveMethod;
import com.carota.util.ConfigHelper;
import com.carota.util.ParcelableStringArray;
import com.carota.vsi.IVehicleDescription;
import com.momock.util.Logger;

public class MainService extends OTAService {

    private static class RequestHandler extends Handler {

        private MainService mContext;

        public RequestHandler(MainService context) {
            super();
            mContext = context;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_WAKEUP:
                    CoreServer.get().wakeup(mContext, (Intent)msg.obj);
                    break;
                case MSG_START:
                    CoreServer.get().start(mContext, mContext.getSecuritySolution(),
                            mContext.getDeploySafety(), mContext.getVehicleDescription());
                    break;
                case MSG_STOP:
                    CoreServer.get().stop(mContext);
                    break;
                case MSG_FILE:
                    String host = null;
                    long reqId = 0;
                    try {
                        if (msg.obj instanceof ParcelableStringArray) {
                            ParcelableStringArray psa = (ParcelableStringArray) msg.obj;
                            reqId = psa.arg;
                            String key = psa.getData()[0];
                            String path = psa.getData()[1];
                            String targetInPackage = psa.getData()[2];
                            host = CoreServer.get().addRemovableStorageFile(mContext, key, path, targetInPackage, msg.arg1);
                        }
                    } catch (Exception e) {
                        Logger.error(e);
                    }

                    if(null != msg.replyTo) {
                        Message replyMsg = Message.obtain();
                        replyMsg.arg1 = msg.arg1;
                        // reply what value:
                        //     -1 : fail;
                        //      0 : disable;
                        //      1 : success;
                        replyMsg.what = -1;
                        if(null != host) {
                            if(host.isEmpty()) {
                                replyMsg.what = 0;
                            } else {
                                replyMsg.what = 1;
                                replyMsg.obj = new ParcelableStringArray(new String[]{host}, reqId);
                            }
                        }
                        if(null == replyMsg.obj) {
                            replyMsg.obj = new ParcelableStringArray(null, reqId);
                        }
                        try {
                            msg.replyTo.send(replyMsg);
                        } catch (RemoteException e) {
                            Logger.error(e);
                        }
                    }


            }
        }
    }

    public static final int MSG_WAKEUP = 0;
    public static final int MSG_START = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_FILE = 3;

    private final Handler mMessageHandler = new RequestHandler(this);

    @Override
    public void onCreate() {
        super.onCreate();
        // add Extra Service
        CoreServer.execute(new Runnable() {
            @Override
            public void run() {
                addExtraService();
            }
        });
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new Messenger(mMessageHandler).getBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mMessageHandler.obtainMessage(MSG_WAKEUP, intent);
        mMessageHandler.sendMessage(msg);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mMessageHandler.sendEmptyMessage(MSG_STOP);
        super.onDestroy();
    }

    private void addExtraService() {
        CoreServer rpcServer = CoreServer.get();
        Context ctx = getApplicationContext();
        ISlaveMethod method = new SlaveMethod();
        ParamRAS paramRAS = ConfigHelper.get(this).get(ParamRAS.class);

        for(ParamRAS.Info info : paramRAS.listInfo()) {
            ISlaveMethod sm = onCreateSlaveMethod(info);
            String pkg = TextUtils.isEmpty(info.getPackage()) ? getPackageName() : info.getPackage();
            ISlaveDownloadAgent sda = new UpdateSlave(info.getId(), info.getHost(),
                    info.getAgent(), pkg, info.getResumeTimeout(), info.getMaxRetry(),
                    null != sm ? sm : method, info.isGuardEnable() ? getSecuritySolution() : null);
            rpcServer.addExtraFunction(new SlaveDownloadAgentService(ctx, sda));
        }
    }

    protected ISlaveMethod onCreateSlaveMethod(ParamRAS.Info info) {
        return null;
    }

    protected ISecuritySolution getSecuritySolution() {
        return null;
    }

    protected IDeploySafety getDeploySafety() {
        return null;
    }

    protected IVehicleDescription getVehicleDescription() {
        return null;
    }
}
