/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.rescue;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.carota.CarotaAnalytics;
import com.carota.CarotaVehicle;
import com.carota.OTAService;
import com.carota.core.ICheckCallback;
import com.carota.core.ICoreStatus;
import com.carota.core.IDownloadCallback;
import com.carota.core.IInstallViewHandler;
import com.carota.core.ISession;
import com.carota.core.remote.IActionMDA;
import com.carota.html.HtmlHelper;
import com.carota.util.LogUtil;
import com.carota.util.MainServiceHolder;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;


public class RescueCarotaClient {

    public interface IInstallViewHandlerFactory {
        IInstallViewHandler create(Context context);
    }

    private static UpdateCore sCore = null;
    private static MainServiceHolder sMainService = null;
    private static IInstallViewHandlerFactory sViewHandlerFactory;
    private static final AtomicBoolean sBootCompleted = new AtomicBoolean(false);
    private static List<String> sLostEcuName = new ArrayList<>();
    private static long sSystemBootTimeout;
    private static long DEFAULT_BOOT_TIMEOUT = 60 * 5 * 1000;

    //todo: add by lipiyan 2023-06-28 for set resume boot mark
    private static Boolean sResume = false;

    public static void init(Context context, IInstallViewHandlerFactory viewHandlerFactory, long bootTimeout) {
        LogUtil.initLogger(context);
        if(null == viewHandlerFactory) {
            throw new RuntimeException("CLIENT Init Fail : Parameters");
        }
        sViewHandlerFactory = viewHandlerFactory;
        sSystemBootTimeout = bootTimeout > 0 ? bootTimeout : DEFAULT_BOOT_TIMEOUT;
        RescueCarotaAnalytics.init(context);
        CarotaVehicle.init(context);
        HtmlHelper.init();
        Intent intent = new Intent("ota.intent.action.RESCUEDAEMON").setPackage(context.getPackageName());
        OTAService.startService(context, intent);
    }

    static void bootStrap(Context context) {
        if(null == sViewHandlerFactory) {
            throw new RuntimeException("CLIENT Error : Need Init");
        }
        if(sBootCompleted.get()) {
            return;
        }

        if(!initLocalSystem(context) && !sCore.syncDataFromMaster()) {
            Logger.debug("CLIENT Boot finish");
            sLostEcuName.clear();
            //todo: add by lipiyan 2023-06-28
            sResume = false;
            sCore.waitRemoteSystemReady(sSystemBootTimeout, sLostEcuName);
            sCore.getCurState().setIsRescue(false);
        } else {
            //todo: add by lipiyan 2023-06-28
            if (sCore.getCurState().getIsRescue()){
                Logger.debug("CLIENT Boot resume: Rescue");
                sResume = true;
                sCore.resumeInstall(sViewHandlerFactory.create(context));
            }else {
                Logger.debug("CLIENT Boot resume: other");
            }
        }
        sBootCompleted.set(true);
    }

    private static boolean initLocalSystem(Context context) {
        Logger.debug("CLIENT Init-Local START");
        if (null == sMainService) {
            sMainService = new MainServiceHolder(context.getPackageName());
        }
        sMainService.start(context);
        sMainService.ensureConnected(context);

        if (null == sCore) {
            Logger.debug("CLIENT Init-Local Create CORE");
            sCore = new UpdateCore(context);
        }
        return sCore.getCurState().isUpgradeTriggered();
    }

    public static void waitMainCtrlReady(String key) {
        try {
            while (null == sCore) {
                Logger.debug("CLIENT Init wait @ " + key);
                Thread.sleep(1000);
            }
            sCore.waitMainCtrlReady(key);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted @ Wait Init");
        }
    }

    public static boolean waitBootComplete(String key) {
        if(null == sViewHandlerFactory) {
            throw new RuntimeException("CLIENT Error : Need Init");
        }

        try {
            while (!sBootCompleted.get()) {
                Logger.debug("CLIENT Boot wait @ " + key);
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

    public static synchronized ISession check(Bundle extra, ICheckCallback callback) throws ExecutionException {
        if(waitBootComplete("CK")) {
            if(sCore.getCurState().isUpgradeTriggered()) {
                throw new ExecutionException("MUST NOT CALL IN UPGRADE : CHECK", null);
            }
            return sCore.check(extra, callback, IActionMDA.CONN_ACTION_CHECK);
        } else {
            return null;
        }
    }

    public static synchronized boolean startDownload(IDownloadCallback callback) throws ExecutionException {
        waitMainCtrlReady("DL-ST");
        if(sCore.getCurState().isUpgradeTriggered()) {
            throw new ExecutionException("MUST NOT CALL IN UPGRADE : START_DL", null);
        }
        return sCore.scheduleDownload(callback);
    }

    public static synchronized boolean stopDownload() throws ExecutionException {
        waitMainCtrlReady("DL-SP");
        if(sCore.getCurState().isUpgradeTriggered()) {
            throw new ExecutionException("MUST NOT CALL IN UPGRADE : STOP_DL", null);
        }
        return sCore.stopDownload();
    }

    @Deprecated
    public static synchronized boolean confirmUpdateValid() {
        waitMainCtrlReady("CFM-UV");
        return !sCore.isSessionExpired(true);
    }

    public static synchronized boolean install(Context context, boolean ignoreExpireConfirmFail) throws ExecutionException {
        waitMainCtrlReady("INS");
        if(sCore.getCurState().isUpgradeTriggered()) {
            throw new ExecutionException("MUST NOT CALL IN UPGRADE : INSTALL", null);
        }
        return sCore.install(sViewHandlerFactory.create(context), ignoreExpireConfirmFail);
    }

    public static ISession getClientSession() {
        waitMainCtrlReady("G-SES");
        return sCore.getCurSession();
    }

    public static ICoreStatus getClientStatus() {
//        waitMainCtrlReady("G-ST");
        return sCore.getCurState();
    }

    public static List<String> getLostEcus() {
        return sLostEcuName;
    }

    public static boolean isBootCompleted() {
        return sBootCompleted.get();
    }

    //todo: add by lipiyan 2023-06-28
    public static boolean isResume(){
        return sResume;
    }

    public static void wakeup(Context context) {
        if (null != sMainService) {
            Logger.debug("CLIENT Wake Active");
            sMainService.start(context);
        } else {
            Logger.error("CLIENT Wake Failure @ Need Init First");
        }
    }


    public static boolean sendUiPoint(int type,String msg) {
        long time = System.currentTimeMillis();
        waitMainCtrlReady("S-POINT");
        return sCore.sendUiPoint(type, time, msg);
    }

    /**
     * send ui event
     *
     * @param upgradeType must use {@link com.carota.core.report.Event.UpgradeType}
     * @param eventCode {@link com.carota.core.report.Event.EventCode}
     * @param msg
     * @param result {@link com.carota.core.report.Event.Result}
     * @return send event result to MDA
     */
    public static boolean sendUiEvent(int upgradeType, int eventCode, String msg, int result) {
        long time = System.currentTimeMillis();
        waitMainCtrlReady("S-EVENT");
        return sCore.sendUiEvent(time,upgradeType,eventCode,msg,result);
    }

    /**
     * Send V2Data to server
     * @param ecu ecu name
     * @param state must use {@link com.carota.mda.telemetry.FotaState.OTA}
     * @param ecuState must use {@link com.carota.mda.telemetry.FotaState.OTA}
     * @param code must use {@link com.carota.mda.telemetry.FotaState.OTA.INTERRUPT}
     * @param errorMsg msg
     */
    public static void sendFotaV2Data(String ecu, int state, int ecuState, int code, String errorMsg) {
        long time = System.currentTimeMillis();
        waitMainCtrlReady("S-EVENT");
        sCore.sendFotaV2Data(ecu, state, ecuState, code, errorMsg, time);
    }

    /**
     *
     * @return true:uat
     * false:defult
     */
    public static boolean getMDAEnvm() {
        waitMainCtrlReady("G-ENVM");
        return sCore.getMDAEnvm();
    }

    /**
     * Set Mda Environment
     * @param isUat true:uat ,false:defult
     * @return
     */
    public static boolean getMDAEnvm(boolean isUat) {
        waitMainCtrlReady("G-ENVM");
        return sCore.setMDAEnvm(isUat);
    }

    public static Boolean getCheckEcuOrNot(){
        return sCore.queryIfCheck();
    }

    public static boolean getStartCheckEcu(){
        return sCore.startVerifyEcu();
    }

    public static int getQueryVerifyEcuResult(){
        return  sCore.queryVerifyEcuResult();
    }
}
