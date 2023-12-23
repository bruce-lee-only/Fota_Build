/*******************************************************************************
 * Copyright (C) 2018-2021 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.mda.remote;


import com.carota.core.ScheduleAttribute;

public interface IActionCMH {
    String GMH_GET_FIELD = "/getfield";
    String GMH_SET_FIELD = "/setfield";

    boolean setScheduleField(String tag, long time, int type, String tid, String track);

    ScheduleAttribute getScheduleField(String tag);

    boolean setErrorWakeUpField(String tag, int code, String desc, String track);
}
