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

import static com.carota.core.VehicleEvent.EVENT_POWER_OFF;

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
import com.carota.mda.remote.info.VehicleDesc;
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

    //todo: mode by lipiyan add "public" for rescue
    public static synchronized void init(Context context) {
        if (null == sVehicle) {
            sVehicle = new CarotaVehicle(context);
        }
    }

    public static IVehicleDetail queryVehicleDetail() {
        CarotaClient.waitMainCtrlReady("G-QY");
        return sVehicle.mActionMDA.queryVehicleDetail(0);
    }

    public static IVehicleDetail queryVehicleVin() {
        CarotaClient.waitMainCtrlReady("G-QY");
        return sVehicle.mActionMDA.queryVehicleDetail(1);
    }

    public static VehicleCondition queryVehicleCondition() {
        IVehicleStatus status = sVehicle.mServMgr.queryStatus();
        return null == status ? null : new VehicleCondition(status);
    }

    public static boolean setScheduleUpgrade(long sec, String id, int type) {
        ISession session = CarotaClient.getClientSession();
        String tid = id;
        if (session != null) {
            tid = session.getScheduleID() + "_" + session.getUSID();
        }
        String track = "0";
        return sVehicle.mEventMgr.setScheduleField(sec, tid, track, type);
    }

    public static boolean setScheduleUpgradeWithoutSession(long sec, String id, int type) {
        String track = "0";
        return sVehicle.mEventMgr.setScheduleField(sec, id, track, type);
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

    /**
     * 远程上电操作
     * author: lipiyan
     * time: 2023-02-07
     * @return
     */
    public static boolean setVehiclePowerOn() {
        return sVehicle.mEventMgr.setPowerOn();
    }

    /**
     * 通知tbox预约升级清理状态
     * @return
     */
    public static boolean cleanStatus(){
        return sVehicle.mEventMgr.cleanStatus();
    }

    /**
     * 上高压操作
     * author: lipiyan
     * time: 2023-06-15
     * @return
     */
    public static boolean setVehicleHVoltageOn() {
        return sVehicle.mEventMgr.setElectricRuntimeEnable(true);
    }

    /**
     * 上高压操作
     * author: lipiyan
     * time: 2023-06-15
     * @return
     */
    public static boolean setVehicleHVoltageOff() {
        return sVehicle.mEventMgr.setElectricRuntimeEnable(false);
    }

    /**
     * 查询vin码fromVsi
     * author: lipiyan
     * time: 2023-07-25
     * @return
     */
    public static VehicleDesc queryVinFromVsi(){
        return sVehicle.mServMgr.queryInfo();
    }

    public static SystemAttribute setSystemAttribute(List<SystemAttribute.Configure> cfg) {
        return sVehicle.mServMgr.setSystemAttribute(cfg);
    }
}
