/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vsi.data;

import android.util.SparseIntArray;

import com.carota.vehicle.IConditionHandler;

import com.carota.vehicle.VehicleService;


public class StatusSnapshot implements IConditionHandler {

    private SparseIntArray mData;

    public StatusSnapshot() {
        mData = new SparseIntArray();
    }

    public void set(int key, int data) {
        mData.put(key, data);
    }

    @Override
    public int getPowerState() {
        return mData.get(VehicleService.KEY_POWER);
    }

    @Override
    public int getGearState() {
        return mData.get(VehicleService.KEY_GEAR);
    }

    @Override
    public int getChargeState() {
        return mData.get(VehicleService.KEY_CHARGE);
    }

    @Override
    public int getSpeed() {
        return mData.get(VehicleService.KEY_SPEED);
    }

    @Override
    public int getBatteryVoltage() {
        return mData.get(VehicleService.KEY_BATTERY_VOL);
    }

    @Override
    public int getBatteryPower() {
        return mData.get(VehicleService.KEY_BATTERY_PWR);
    }

    @Override
    public int getBatteryLevel() {
        return mData.get(VehicleService.KEY_BATTERY_LEVEL);
    }

    @Override
    public int getHandbrakeState() {
        return mData.get(VehicleService.KEY_HANDBRAKE);
    }

    @Override
    public int getEngineState() {
        return mData.get(VehicleService.KEY_ENGINE);
    }

    @Override
    public int getMotorState() {
        return mData.get(VehicleService.KEY_MOTOR);
    }

    @Override
    public int getDiagnoseState() {
        return mData.get(VehicleService.KEY_DIAGNOSE);
    }

    @Override
    public int getTelDiagnoseState() {
        return mData.get(VehicleService.KEY_TEL_DIAGNOSE);
    }

    @Override
    public int getVehicleModeState() {
        return mData.get(VehicleService.KEY_VEHICLE_MODE);
    }

    @Override
    public int getLockState() {
        return mData.get(VehicleService.KEY_LOCK);
    }

    @Override
    public int getWindowState() {
        return mData.get(VehicleService.KEY_WINDOW);
    }

    @Override
    public int getSecurityState() {
        return mData.get(VehicleService.KEY_SECURITY);
    }

    @Override
    public int getHvReadyState() {
        return mData.get(VehicleService.KEY_HV_READY);
    }

    @Override
    public int getVtolState() {
        return mData.get(VehicleService.KEY_VTOL);
    }

    @Override
    public int getPetMode() {
        return mData.get(VehicleService.KEY_PET_MODE);
    }

    @Override
    public int getSentinelMode() {
        return mData.get(VehicleService.KEY_SENTINEL_MODE);
    }

    @Override
    public int getDcdcMode() {
        return mData.get(VehicleService.KEY_DCDC_MODE);
    }

    @Override
    public int getOtaMode() {
        return mData.get(VehicleService.KEY_OTA_MODE);
    }
}
