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

import com.carota.build.IConfiguration;
import com.carota.build.ParamDM;
import com.carota.build.ParamExternalHttpProxy;
import com.carota.build.ParamHttpProxy;
import com.carota.build.ParamHub;
import com.carota.build.ParamLocal;
import com.carota.build.ParamMDA;
import com.carota.build.ParamRSM;
import com.carota.build.ParamVSI;
import com.carota.dm.file.IFileManager;
import com.carota.dm.file.ftp.FtpFileManager;
import com.carota.dm.file.local.LocalFileManager;
import com.carota.dm.provider.DownloadManagerService;
import com.carota.dm.task.ITaskManager;
import com.carota.dm.task.TaskManager;
import com.carota.httpproxy.ActionProxy;
import com.carota.httpproxy.HttpProxyService;
import com.carota.hub.provider.HubService;
import com.carota.mda.deploy.IDeploySafety;
import com.carota.mda.provider.MasterDownloadAgentService;
import com.carota.mda.security.ISecuritySolution;
import com.carota.rsm.RemovableStorageManager;
import com.carota.rsm.provider.RemovableStorageManagerService;
import com.carota.svr.IRouterHttp;
import com.carota.svr.PrivReqHelper;
import com.carota.svr.RouterHttp;
import com.carota.svr.RouterService;
import com.carota.util.ConfigHelper;
import com.carota.util.HttpHelper;
import com.carota.util.HubNotify;
import com.carota.util.SerialExecutor;
import com.carota.vsi.IVehicleDescription;
import com.carota.vsi.provider.VehicleInformationService;
import com.momock.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoreServer {

    public static final int FILE_TYPE_RAW = 0;
    public static final int FILE_TYPE_ZIP = 1;
    public static final int FILE_TYPE_XOR = 2;

    private final SerialExecutor mExecutor;
    private final Map<String, RouterService> mFuncPool;
    private final Map<String, RouterService> mExtraPool;
    private IRouterHttp mHttpServer;
    private HubNotify mNotify;

    private static CoreServer sCore = new CoreServer();

    public static CoreServer get() {
        return sCore;
    }

    private CoreServer() {
        mExecutor = new SerialExecutor();
        mFuncPool = new HashMap<>();
        mExtraPool = new HashMap<>();
        mHttpServer = null;
        mNotify = null;
    }

    public static void execute(Runnable r) {
        get().mExecutor.execute(r);
    }

    public boolean isRunning() {
        return null != mHttpServer && mHttpServer.isRunning();
    }

    public void stop(final Context context) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                stopSync(context.getApplicationContext());
            }
        });
    }

    private synchronized void stopSync(Context context) {
        if (null != mNotify) {
            mNotify.stop();
            mNotify = null;
            Logger.debug("rpc-svr : STOP-NOTI");
        }
        if (null != mHttpServer) {
            for (RouterService rs : mFuncPool.values()) {
                rs.onStop(mHttpServer);
            }
            mHttpServer.stopServer();
            mHttpServer = null;
            Logger.debug("rpc-svr : STOP-SVR");
        }
        Logger.debug("rpc-svr : STOPPED");
    }

    public void wakeup(final Context context, final Intent intent){
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                wakeupSync(context.getApplicationContext(), intent);
            }
        });
    }

    private void wakeupSync(Context context, Intent intent) {
        if(null != mHttpServer) {
            Logger.debug("rpc-svr : WAKEUP");
            for (RouterService rs : mFuncPool.values()) {
                rs.onWakeUp(mHttpServer, intent);
            }
        }
    }

    public void start(final Context context, final ISecuritySolution method,
                      final IDeploySafety safety, final IVehicleDescription description) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if(!isRunning()) {
                    startSync(context.getApplicationContext(), method, safety, description);
                } else {
                    Logger.debug("rpc-svr : STARTED");
                }
            }
        });
    }

    private synchronized void startSync(Context ctx, ISecuritySolution method,
                                        IDeploySafety safety, IVehicleDescription description) {
        try {
            IConfiguration cfg = ConfigHelper.get(ctx);
            ParamHub paramHub = cfg.get(ParamHub.class);
            Logger.debug("SET SERVICE PROXY");
            PrivReqHelper.setGlobalProxy(paramHub.getAddr(), paramHub.getPort());

            ParamExternalHttpProxy eProxy = cfg.get(ParamExternalHttpProxy.class);
            if (eProxy.isEnabled()) {
                HttpHelper.setExternalProxy(
                        new ActionProxy(eProxy.getHost(),
                                eProxy.getWhiteList(),
                                eProxy.isWhiteListEnabled()));
                Logger.debug("Set External Proxy");
            }

            ParamLocal local = cfg.get(ParamLocal.class);
            mHttpServer = new RouterHttp(ctx, local.getPort());
            mFuncPool.clear();
            mFuncPool.putAll(mExtraPool);
            HubService hubSvc = initServer(ctx, mFuncPool, cfg, method, safety, description);
            List<String> registerAtHub = new ArrayList<>();
            for (RouterService rs : mFuncPool.values()) {
                rs.onInit(mHttpServer);
                if(null != hubSvc) {
                    hubSvc.addLocalServiceHost(rs.toString());
                }
                if (rs.needNotifyHub()) {
                    registerAtHub.add(rs.toString());
                }
            }
            Logger.debug("rpc-svr : START");
            mHttpServer.startServer();
            for (RouterService rs : mFuncPool.values()) {
                rs.onStart(mHttpServer);
            }

            Logger.debug("rpc-svr : P = %d", mHttpServer.getPort());
            ParamHub hub = cfg.get(ParamHub.class);
            if(hub.isEnabled()) {
                Logger.debug("rpc-svr : HUB @ Local : %s", Arrays.toString(registerAtHub.toArray(new String[0])));
            } else {
                Logger.debug("rpc-svr : Notify-HUB");
                if (null != mNotify) {
                    mNotify.stop();
                }
                mNotify = new HubNotify(hub.getHost(), mHttpServer.getPort());
                mNotify.update(registerAtHub);
                mNotify.start();
            }
        } catch (Exception e) {
            Logger.error(e);
        }
        Logger.debug("rpc-svr : READY");
    }

    private HubService initServer(Context ctx, Map<String, RouterService> funcMap,
                                  IConfiguration cfg, ISecuritySolution method,
                                  IDeploySafety safety, IVehicleDescription description) {
        ParamDM paramDM = cfg.get(ParamDM.class);
        RouterService rs;
        for (ParamDM.Info info:paramDM.getDmList()) {
            if (info != null) {
                rs = new DownloadManagerService(ctx, info.getHost(),getTaskManager(paramDM.getDownloadDir(ctx),info));
                funcMap.put(rs.toString(), rs);
                Logger.debug("rpc-svr : INIT-DM @%s",info.getHost());
            } else {
                Logger.debug("rpc-svr : INIT-DM Error");
            }
        }

        ParamMDA paramMDA = cfg.get(ParamMDA.class);
        if(paramMDA.isEnabled()) {
            rs = new MasterDownloadAgentService(ctx, paramMDA.getHost(), method, safety, description);
            funcMap.put(rs.toString(), rs);
            Logger.debug("rpc-svr : INIT-MDA");
        }

        ParamVSI paramVSI = cfg.get(ParamVSI.class);
        if(paramVSI.isEnabled()) {
            rs = new VehicleInformationService(ctx, paramVSI.getHost());
            funcMap.put(rs.toString(), rs);
            Logger.debug("rpc-svr : INIT-VSI");
        }

        ParamRSM paramRSM = cfg.get(ParamRSM.class);
        if(paramRSM.isEnabled()) {
            rs = new RemovableStorageManagerService(paramRSM.getHost());
            funcMap.put(rs.toString(), rs);
            Logger.debug("rpc-svr : INIT-RSM");
        }

        ParamHttpProxy paramHttpProxy = cfg.get(ParamHttpProxy.class);
        if(paramHttpProxy.isEnabled()) {
            rs = new HttpProxyService(paramHttpProxy.getHost());
            funcMap.put(rs.toString(), rs);
            Logger.debug("rpc-svr : INIT-HPS");
        }

        HubService hub = null;
        ParamHub paramHub = cfg.get(ParamHub.class);
        if(paramHub.isEnabled()) {
            hub = new HubService(paramHub.getHost());
            funcMap.put(hub.toString(), hub);
            Logger.debug("rpc-svr : INIT-HUB");
        }
        return hub;
    }

    public void addExtraFunction(RouterService agent) {
        mExtraPool.put(agent.toString(), agent);
    }

    public String addRemovableStorageFile(Context ctx, String id, String filePath, String extra, int fileType) {
        RemovableStorageManager rsm = RemovableStorageManager.get();
//        if(reset) {
//            rsm.reset();
//        }

        Logger.debug("rpc-svr : ADD-RSF (" + fileType + ") [" + id + "] " + filePath + " @ " + extra);

        boolean addRet;
        if(FILE_TYPE_XOR == fileType) {
            addRet = rsm.addXorFile(new File(filePath), id, extra);
        } else {
            addRet = rsm.addZipFile(new File(filePath), id, extra);
        }
        if (addRet) {
            ParamRSM paramRSM = ConfigHelper.get(ctx).get(ParamRSM.class);
            if(paramRSM.isEnabled()) {
                Logger.debug("rpc-svr : ADD-RSF OK @ " + id);
                return paramRSM.getHost();
            } else {
                Logger.debug("rpc-svr : ADD-RSF Disable");
                return "";
            }
        }
        Logger.debug("rpc-svr : ADD-RSF Failure");
        return null;
    }

    public ITaskManager getTaskManager(File downloadDir, ParamDM.Info info) {
        IFileManager manager;
        if (info.isFtp()) {
            manager = FtpFileManager.newInstance(info);
        } else {
            manager = new LocalFileManager(downloadDir, info.getType());
        }
        return new TaskManager(manager, info.getRetry(), info.getLimitTime(),info.getReserveSpace());
    }
}
