/*******************************************************************************
 * Copyright (C) 2018-2021 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/
package com.carota.core;


public class ScheduleAttribute {
    public static final int TYPE_IDLE = -1;
    public static final int TYPE_CANCEL = 0;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_FACTORY = 2;
    public static final int TYPE_TEAM = 3;

    public long scheduleTime = -1;
    public long scheduleType = TYPE_CANCEL;
    public String scheduleTid = "UNKNOWN";
    public int scheduleGroup = 1;
    public String scheduleTrack = "UNKNOWN";
}
