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
import android.os.Bundle;

import com.carota.cmh.ControlMessageHandler;
import com.carota.mda.remote.IActionCMH;
import com.carota.mda.remote.IActionVSI;
import com.carota.util.ReqTag;
import com.carota.vsi.VehicleServiceManager;

public class VehicleEvent {

    public static final String EVENT_SCHEDULE = "ScheduleUpgrade";
    public static final String EVENT_RUNTIME = "UpgradeRuntime";
    public static final String EVENT_ELECTRIC_RUNTIME = "ElectricUpgradeRuntime";
    public static final String EVENT_POWER_OFF = "PowerOff";
	public static final String EVENT_BEAT = "Beat";

    private static final String ACTION_ON = "ON";
    private static final String ACTION_OFF = "OFF";

    public static final int STATUS_IDLE = 0;
    public static final int STATUS_RECEIVED = 10;
    public static final int STATUS_DOWNLOAD = 20;
    public static final int STATUS_READY = 30;
    public static final int STATUS_USING = 31;
    public static final int STATUS_INSTALL = 40;
    public static final int STATUS_SUCCESS = 100;
    public static final int STATUS_ERROR = 101;
    public static final int STATUS_FAIL = 102;
    private IActionVSI mActionVSI;
    private IActionCMH mActionCMH;

    private static VehicleEvent sMaster = null;

    public static VehicleEvent get(Context context) {
        synchronized (VehicleEvent.class) {
            if(null == sMaster) {
                sMaster = new VehicleEvent(context);
            }
        }
        return sMaster;
    }

    private VehicleEvent(Context context) {
        mActionVSI = new VehicleServiceManager(context);
        mActionCMH = new ControlMessageHandler(context);
    }

//    public boolean scheduleUpgrade(long sec) {
//        if(sec >= 0) {
//            return mActionVSI.fireEvent(EVENT_SCHEDULE, sec, null);
//        } else {
//            return mActionVSI.removeEvent(EVENT_SCHEDULE);
//        }
//    }

    public boolean setScheduleField(long sec, String tid, String track) {
        if (sec >= 0) {
            return mActionCMH.setScheduleField(ReqTag.TAG_SRC_MDA,
                    sec,
                    ScheduleAttribute.TYPE_NORMAL,
                    tid,
                    track);
        } else {
            return mActionCMH.setScheduleField(ReqTag.TAG_SRC_MDA,
                    sec,
                    ScheduleAttribute.TYPE_CANCEL,
                    tid,
                    track);
        }
    }

    public boolean setScheduleFieldIdle(String tid, String track) {
        return mActionCMH.setScheduleField(ReqTag.TAG_SRC_MDA,
                -1,
                ScheduleAttribute.TYPE_IDLE,
                tid,
                track);
    }

    public ScheduleAttribute getScheduleField() {
        return mActionCMH.getScheduleField(ReqTag.TAG_SRC_MDA);
    }

    public boolean setErrorWakeUpField(int code, String desc, String track) {
        return mActionCMH.setErrorWakeUpField(ReqTag.TAG_SRC_MDA, code, desc, track);
    }

    public boolean setPowerOff() {
        return mActionVSI.fireEvent(EVENT_POWER_OFF,0,null);
    }

    public boolean setUpgradeRuntimeEnable(boolean enable) {
        Bundle bundle = new Bundle();
        bundle.putString("action", enable ? ACTION_ON : ACTION_OFF);
        if (!enable) bundle.putBoolean("dtc", true);
        return mActionVSI.fireEvent(EVENT_RUNTIME, 0, bundle);
    }

    public boolean setElectricRuntimeEnable(boolean enable) {
        Bundle bundle = new Bundle();
        bundle.putString("action", enable ? ACTION_ON : ACTION_OFF);
        return mActionVSI.fireEvent(EVENT_ELECTRIC_RUNTIME, 0, bundle);
    }

    private String getEventHandlerKey(String action, String event) {
        return event + action;
    }
}
