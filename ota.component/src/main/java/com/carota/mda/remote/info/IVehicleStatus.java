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

    int STATE_VEHICLE_FACTORY = 0;
    int STATE_VEHICLE_TRANSPORT = 1;
    int STATE_VEHICLE_NORMAL = 2;
    int STATE_VEHICLE_UNRECOGNIZED = 3;
    int STATE_VEHICLE_DYNO = 4;
    int STATE_VEHICLE_CRASH = 5;
    int STATE_VEHICLE_FACTORY_PAUSED = 6;
    int STATE_VEHICLE_TRANSPORT_PAUSED = 7;
    int getVehicleModeState();

    int STATE_LOCK_OFF = 0;
    int STATE_LOCK_ON = 1;
    int getLockState();

    int STATE_WINDOW_CLOSED = 0;
    int STATE_WINDOW_OPENED = 1;
    int getWindowState();

    int STATE_SECURITY_OFF = 0;
    int STATE_SECURITY_ON = 1;
    int getSecurityState();

	int getHvReadyState();

    int getVtolState();

	int PET_MODE_OFF = 0;
    int PET_MODE_ON = 1;
    int getPetMode();
	
    int isMotor();

	int SENTINEL_MODE_OFF = 0;
    int SENTINEL_MODE_ON = 1;
    int getSentinelMode();
}
