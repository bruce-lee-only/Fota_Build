/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vehicle;

public interface IConditionHandler {
    // WARN: THE STATE VALUE BELOW IS RELATED TO "VehicleStatusInformation.prop" FILE, DO NOT CHANGE IT.
    int STATE_UNKNOWN = -1;

    int STATE_POWER_OFF = 0;
    int STATE_POWER_ACC = 1;
    int STATE_POWER_ON = 2;
    int STATE_POWER_START = 3;
    int getPowerState();

    int STATE_GEAR_P = 0;
    int STATE_GEAR_N = 1;
    int STATE_GEAR_D = 2;
    int STATE_GEAR_R = 3;
    int getGearState();

    int STATE_CHARGE_OFF = 0;
    int STATE_CHARGE_IDLE = 1;
    int STATE_CHARGE_SLOW = 2;
    int STATE_CHARGE_FAST = 3;
    int getChargeState();

    int getSpeed();    // m/s

    int getBatteryVoltage();   // mv

    int getBatteryPower();    // 0 - 100 %

    int getBatteryLevel(); // 0 - 100 % storage battery energy

    int STATE_HANDBRAKE_OFF = 0;
    int STATE_HANDBRAKE_ON = 1;
    int getHandbrakeState();

    int STATE_ENGINE_OFF = 0;
    int STATE_ENGINE_ON = 1;
    int getEngineState();

    int STATE_MOTOR_OFF = 0;
    int STATE_MOTOR_READY = 1;
    int getMotorState();

    int STATE_DIAGNOSE_OFF = 0;
    int STATE_DIAGNOSE_CONNECTED = 1;
    int getDiagnoseState();

    int STATE_TEL_DIAGNOSE_NOT_READ = 0;
    int STATE_TEL_DIAGNOSE_READ = 1;
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

    int STATE_HV_READY_OFF = 0;
    int STATE_HV_READY_ON = 1;
    int getHvReadyState();

    int STATE_VTOL_OFF = 0;
    int STATE_VTOL_ON = 1;
    int getVtolState();
    int PET_MODE_OFF = 0;
    int PET_MODE_ON = 1;
    int getPetMode();

    int SENTINEL_MODE_OFF = 0;
    int SENTINEL_MODE_ON = 1;
    int getSentinelMode();
}
