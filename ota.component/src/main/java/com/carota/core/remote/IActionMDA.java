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
import java.util.Map;

public interface IActionMDA {
    String CONN_ACTION_CHECK = "check";
    String CONN_ACTION_SYNC = "sync";
    String CONN_ACTION_VERIFY = "verify";
    String CONN_ACTION_FACTORY = "factory";

    String ENV_ACTION_DEFAULT = "default";
    String ENV_ACTION_UAT = "uat";

    /**
     * getConfigDownloadUrl keys
     */
    public static final String CONFIG_PRE_CON = "precon";
    public static final String CONFIG_CC_TEXT = "cc_text";
    UpdateSession connect(String action, Bundle extra);
    VehicleInfo queryVehicleDetail(int flag);

    InstallProgress queryUpdateStatus();

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

    Map<String, String> getConfigFiles(String rootDir, List<String> keys);
}
