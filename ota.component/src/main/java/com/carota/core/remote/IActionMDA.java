/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core.remote;

import android.os.Bundle;

import com.carota.core.data.UpdateSession;
import com.carota.core.remote.info.DownloadProgress;
import com.carota.core.remote.info.InstallProgress;
import com.carota.core.remote.info.MDAInfo;
import com.carota.core.remote.info.VehicleInfo;

import java.util.List;

public interface IActionMDA {

    String CONN_ACTION_CHECK = "check";
    String CONN_ACTION_SYNC = "sync";
    String CONN_ACTION_VERIFY = "verify";
    String CONN_ACTION_FACTORY = "factory";

    String CONN_ACTION_RESCUE = "rescue";

    String ENV_ACTION_DEFAULT = "default";
    String ENV_ACTION_UAT = "uat";
    String ENV_ACTION_TEST = "test";

    UpdateSession connect(String action, Bundle extra);
    VehicleInfo queryVehicleDetail(int flag);

    InstallProgress queryUpdateStatus();

    boolean upgradeEcuInSlave(String usid) throws InterruptedException;
    boolean upgradeEcuInMaster(String usid) throws InterruptedException;

    boolean downloadPackageStart(String usid);
    boolean downloadPackageStop();
    DownloadProgress downloadPackageQuery() throws InterruptedException;

    boolean checkAlive();

    boolean checkSystemReady(List<String> lostEcus);

    MDAInfo syncMasterStatus();

    List<String> syncUpdateCondition();

    String syncMasterEnvm();

    boolean setMasterEnvm(boolean isUat);

    boolean sendPointData(int type,long time,String msg);

    boolean sendEventData(long time, int upgradeType, int code, String msg, int result,String scheduleID,int eic);

    boolean sendFotaV2Data(String ecu, int state, int ecustate, int code, String erMsg, long time);

    String EVENT_RESCUE_QUERY = "RescueQuery";
    String EVENT_RESCUE_VERIFY = "RescueVerify";
    String EVENT_RESCUE_RESULT = "RescueResult";
    int fireRescue(String action, Bundle bundle);

    boolean eCallEvent();

    boolean updateTimeOut();

}
