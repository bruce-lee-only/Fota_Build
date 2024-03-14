/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote.info;

import com.carota.vehicle.IConditionHandler;

public interface IVehicleStatus {

    int STATE_UNKNOWN = IConditionHandler.STATE_UNKNOWN;

    int STATE_POWER_OFF = IConditionHandler.STATE_POWER_OFF;
    int STATE_POWER_ACC = IConditionHandler.STATE_POWER_ACC;
    int STATE_POWER_ON = IConditionHandler.STATE_POWER_ON;
    int STATE_POWER_START = IConditionHandler.STATE_POWER_START;

    int STATE_GEAR_P = IConditionHandler.STATE_GEAR_P;
    int STATE_GEAR_N = IConditionHandler.STATE_GEAR_N;
    int STATE_GEAR_D = IConditionHandler.STATE_GEAR_D;
    int STATE_GEAR_R = IConditionHandler.STATE_GEAR_R;

    int STATE_CHARGE_OFF = IConditionHandler.STATE_CHARGE_OFF;
    int STATE_CHARGE_IDLE = IConditionHandler.STATE_CHARGE_IDLE;
    int STATE_CHARGE_SLOW = IConditionHandler.STATE_CHARGE_SLOW;
    int STATE_CHARGE_FAST = IConditionHandler.STATE_CHARGE_FAST;

    int STATE_HANDBRAKE_OFF = IConditionHandler.STATE_HANDBRAKE_OFF;
    int STATE_HANDBRAKE_ON = IConditionHandler.STATE_HANDBRAKE_ON;

    int STATE_READY_ON = IConditionHandler.STATE_ENGINE_OFF;
    int STATE_READY_OFF = IConditionHandler.STATE_ENGINE_ON;

    int STATE_DIAGNOSE_OFF = IConditionHandler.STATE_DIAGNOSE_OFF;
    int STATE_DIAGNOSE_CONNECTED = IConditionHandler.STATE_DIAGNOSE_CONNECTED;

    int STATE_HV_READY_OFF =  IConditionHandler.STATE_HV_READY_OFF;
    int STATE_HV_READY_ON = IConditionHandler.STATE_HV_READY_ON;

    int STATE_VTOL_OFF =  IConditionHandler.STATE_VTOL_OFF;
    int STATE_VTOL_ON = IConditionHandler.STATE_VTOL_ON;

    int STATE_SECURITY_ON = IConditionHandler.STATE_SECURITY_ON;
    int STATE_SECURITY_OFF = IConditionHandler.STATE_SECURITY_OFF;

    int getPowerState();

    int getGearState();

    int getChargeState();

    int getSpeed();

    int getBatteryVoltage();

    int getBatteryPower();

    int getBatteryLevel(); // 0 - 100 % storage battery energy

    int isPowerReady();

    int isHandbrakeOn();

    int getDiagnoseState();

    int getTelDiagnoseState();

    int STATE_VEHICLE_FACTORY = IConditionHandler.STATE_VEHICLE_FACTORY;
    int STATE_VEHICLE_TRANSPORT = IConditionHandler.STATE_VEHICLE_TRANSPORT;
    int STATE_VEHICLE_NORMAL = IConditionHandler.STATE_VEHICLE_NORMAL;
    int STATE_VEHICLE_UNRECOGNIZED = IConditionHandler.STATE_VEHICLE_UNRECOGNIZED;
    int STATE_VEHICLE_DYNO = IConditionHandler.STATE_VEHICLE_DYNO;
    int STATE_VEHICLE_CRASH = IConditionHandler.STATE_VEHICLE_CRASH;
    int STATE_VEHICLE_FACTORY_PAUSED = IConditionHandler.STATE_VEHICLE_FACTORY_PAUSED;
    int STATE_VEHICLE_TRANSPORT_PAUSED = IConditionHandler.STATE_VEHICLE_TRANSPORT_PAUSED;
    int getVehicleModeState();
    int STATE_LOCK_OFF = IConditionHandler.STATE_LOCK_OFF;
    int STATE_LOCK_ON = IConditionHandler.STATE_LOCK_ON;
    int getLockState();

    int STATE_WINDOW_CLOSED = IConditionHandler.STATE_WINDOW_CLOSED;
    int STATE_WINDOW_OPENED = IConditionHandler.STATE_WINDOW_OPENED;
    int getWindowState();

    int getSecurityState();

    int getHvReadyState();

    int getVtolState();

    int PET_MODE_OFF = IConditionHandler.PET_MODE_OFF;
    int PET_MODE_ON = IConditionHandler.PET_MODE_ON;
    int getPetMode();

    int SENTINEL_MODE_OFF = IConditionHandler.SENTINEL_MODE_OFF;
    int SENTINEL_MODE_ON = IConditionHandler.SENTINEL_MODE_ON;
    int getSentinelMode();

    int DCDC_MODE_OFF = IConditionHandler.DCDC_MODE_OFF;
    int DCDC_MODE_ON = IConditionHandler.DCDC_MODE_ON;
    int getDcdcMode();

    int OTA_MODE_OFF = IConditionHandler.OTA_MODE_OFF;
    int OTA_MODE_ON = IConditionHandler.OTA_MODE_ON;
    int getOtaMode();
}
