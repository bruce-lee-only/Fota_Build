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

import android.content.BroadcastReceiver;
import android.content.Context;

import com.carota.build.IConfiguration;
import com.carota.core.ISession;
import com.carota.core.IVehicleDetail;
import com.carota.core.ScheduleAttribute;
import com.carota.core.UpdateCore;
import com.carota.core.VehicleCondition;
import com.carota.core.VehicleEvent;
import com.carota.core.remote.IActionMDA;
import com.carota.mda.remote.info.IVehicleStatus;
import com.carota.core.SystemAttribute;
import com.carota.util.ConfigHelper;
import com.carota.vsi.VehicleServiceManager;
import com.momock.util.Logger;

import java.util.List;

public class CarotaVehicle {

    private static CarotaVehicle sVehicle = null;
    private final VehicleServiceManager mServMgr;
    private final IActionMDA mActionMDA;
    private final VehicleEvent mEventMgr;

    private CarotaVehicle(Context context) {
        IConfiguration cfg = ConfigHelper.get(context);
        mActionMDA = UpdateCore.createActionMDA(cfg);
        mServMgr = new VehicleServiceManager(context);
        mEventMgr = VehicleEvent.get(context);
    }

    static synchronized void init(Context context) {
        if (null == sVehicle) {
            sVehicle = new CarotaVehicle(context);
        }
    }

    /**
     *
     * @param flag
     *         ALL = 0;
     *         VEHICLE = 1;
     *         VERSION = 2;
     * @return
     */
    public static IVehicleDetail queryVehicleDetail(int flag) {
        CarotaClient.waitMainCtrlReady("G-QY");
        return sVehicle.mActionMDA.queryVehicleDetail(flag);
    }

    public static VehicleCondition queryVehicleCondition() {
        IVehicleStatus status = sVehicle.mServMgr.queryStatus();
        return null == status ? null : new VehicleCondition(status);
    }

    public static boolean setScheduleUpgrade(long sec) {
        ISession session = CarotaClient.getClientSession();
        String tid = "" + System.currentTimeMillis();
        if (session != null) {
            tid = session.getScheduleID();
        }
        String track = "0";
        return sVehicle.mEventMgr.setScheduleField(sec, tid, track);
    }

    public static boolean setScheduleIdle() {
        ISession session = CarotaClient.getClientSession();
        String tid = "" + System.currentTimeMillis();
        if (session != null) {
            tid = session.getScheduleID();
        }
        String track = "0";
        return sVehicle.mEventMgr.setScheduleFieldIdle(tid, track);
    }

    public static ScheduleAttribute getSchedule() {
        return sVehicle.mEventMgr.getScheduleField();
    }

    public static boolean setErrorWakeUp(int code, String desc) {
        String track = "" + System.currentTimeMillis();
        return sVehicle.mEventMgr.setErrorWakeUpField(code, desc, track);
    }

    @Deprecated
    public static boolean isUpgradeScheduled() {
        ScheduleAttribute schedule = getSchedule();
        return schedule != null && schedule.scheduleType == ScheduleAttribute.TYPE_NORMAL && schedule.scheduleTime < System.currentTimeMillis();
    }

    public static boolean setUpgradeRuntimeEnable(boolean enable, boolean powerSystemOff) {
        VehicleEvent panel = sVehicle.mEventMgr;
        if (powerSystemOff && !panel.setElectricRuntimeEnable(enable) && enable) {
            return false;
        }
        return panel.setUpgradeRuntimeEnable(enable);
    }

    public static boolean setVehiclePowerOff() {
        return sVehicle.mEventMgr.setPowerOff();
    }

    public static SystemAttribute setSystemAttribute(List<SystemAttribute.Configure> cfg) {
        return sVehicle.mServMgr.setSystemAttribute(cfg);
    }
}
