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

import android.util.SparseIntArray;

public class VehicleConditionCache {

    private SparseIntArray mCache = new SparseIntArray();
    private IConditionHandler mHandler = new IConditionHandler() {
        @Override
        public int getPowerState() {
            return mCache.get(VehicleService.KEY_POWER, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getGearState() {
            return mCache.get(VehicleService.KEY_GEAR, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getChargeState() {
            return mCache.get(VehicleService.KEY_CHARGE, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getSpeed() {
            return mCache.get(VehicleService.KEY_SPEED, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getBatteryVoltage() {
            return mCache.get(VehicleService.KEY_BATTERY_VOL, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getBatteryPower() {
            return mCache.get(VehicleService.KEY_BATTERY_PWR, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getBatteryLevel() {
            return mCache.get(VehicleService.KEY_BATTERY_LEVEL, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getHandbrakeState() {
            return mCache.get(VehicleService.KEY_HANDBRAKE, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getEngineState() {
            return mCache.get(VehicleService.KEY_ENGINE, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getMotorState() {
            return mCache.get(VehicleService.KEY_MOTOR, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getDiagnoseState() {
            return mCache.get(VehicleService.KEY_DIAGNOSE,IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getTelDiagnoseState() {
            return mCache.get(VehicleService.KEY_TEL_DIAGNOSE,IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getVehicleModeState() {
            return mCache.get(VehicleService.KEY_VEHICLE_MODE,IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getLockState() {
            return mCache.get(VehicleService.KEY_LOCK, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getWindowState() {
            return mCache.get(VehicleService.KEY_WINDOW, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getSecurityState() {
            return mCache.get(VehicleService.KEY_SECURITY, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getHvReadyState() {
            return mCache.get(VehicleService.KEY_HV_READY);
        }

        @Override
        public int getVtolState() {
            return mCache.get(VehicleService.KEY_VTOL);
        }

        @Override
        public int getPetMode() {
            return mCache.get(VehicleService.KEY_PET_MODE, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getSentinelMode() {
            return mCache.get(VehicleService.KEY_SENTINEL_MODE, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getDcdcMode() {
            return mCache.get(VehicleService.KEY_DCDC_MODE, IConditionHandler.STATE_UNKNOWN);
        }

        @Override
        public int getOtaMode() {
            return mCache.get(VehicleService.KEY_OTA_MODE, IConditionHandler.STATE_UNKNOWN);
        }
    };

    public IConditionHandler getHandler() {
        return mHandler;
    }

    public void update(int key, int value) {
        mCache.append(key, value);
    }

    public void setPowerState(int state) {
        update(VehicleService.KEY_POWER, state);
    }

    public void setGearState(int state) {
        update(VehicleService.KEY_GEAR, state);
    }

    public void setChargeState(int state) {
        update(VehicleService.KEY_CHARGE, state);
    }

    public void setSpeed(int meterPerSecond) {
        update(VehicleService.KEY_SPEED, meterPerSecond);
    }

    public void setBatteryVoltage(int microVoltage) {
        update(VehicleService.KEY_BATTERY_VOL, microVoltage);
    }

    public void setBatteryPower(int percent) {
        update(VehicleService.KEY_BATTERY_PWR, percent);
    }

    public void setBatteryLevel(int percent) {update(VehicleService.KEY_BATTERY_LEVEL, percent);}

    public void setHandbrakeState(int state) {
        update(VehicleService.KEY_HANDBRAKE, state);
    }

    public void setEngineState(int state) {
        update(VehicleService.KEY_ENGINE, state);
    }

    public void setMotorState(int state) {
        update(VehicleService.KEY_MOTOR, state);
    }

    public void setDiagnoseState(int state) {
        update(VehicleService.KEY_DIAGNOSE, state);
    }

    public void setTelDiagnoseState(int state) {
        update(VehicleService.KEY_TEL_DIAGNOSE, state);
    }

    public void setVehicleModeState(int state) {
        update(VehicleService.KEY_VEHICLE_MODE, state);
    }

    public void setLockState(int state) {
        update(VehicleService.KEY_LOCK, state);
    }

    public void setWindowState(int state) {
        update(VehicleService.KEY_WINDOW, state);
    }

    public void setSecurityState(int state) {
        update(VehicleService.KEY_SECURITY, state);
    }
}
