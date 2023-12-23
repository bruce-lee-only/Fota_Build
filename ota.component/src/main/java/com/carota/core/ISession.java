/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core;

import android.content.Context;

import org.json.JSONObject;

import java.util.List;

public interface ISession {

    String MODE_AUTO_DOWNLOAD = "auto-download";
    String MODE_AUTO_INSTALL = "auto-update";
    String MODE_USER_CONFIRM = "by-user";
    String MODE_USER_LIMIT = "by-user-limit";
    String MODE_AUTO_INSTALL_SILENT = "auto-update-silent";
    String MODE_AUTO_INSTALL_SCHEDULE = "auto-update-schedule";
    String MODE_AUTO_UPDATE_FACTORY = "auto-update-factory";
    //todo: add by lipiyan 2023-06-24 for 增加远程救援模式
    String MODE_RESCUE = "rescue";

    String OPERATE_EIC_OFF = "eic_system_off";
    String OPERATE_ACC_LOCK = "power_status_lock";

    int getTaskCount();
    ITask getTask(int index);
    ITask getTask(String id);
    String getVinCode();
    String getUSID();
    List<String> getCondition();
    String getMode();
    String getReleaseNote();
    List<String> getOperation();
    String getCampaignID();
    String getScheduleID();
    long getAppointmentTimeLeft();
    long getUpdateTime();

    IDisplayInfo getDisplayInfo(Context context);
}
