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

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import java.util.Map;

/**
 *     <service
 *             android:name=".AgentService"
 *             android:permission="ota.permission.VEHICLE">
 *             <intent-filter>
 *                 <action android:name="ota.intent.action.BIND_VS" />
 *             </intent-filter>
 *         </service>
 *     <permission
 *         android:name="ota.permission.VEHICLE"
 *         android:protectionLevel="signatureOrSystem" />
 */
public abstract class VehicleService extends Service {

    public static final int FLAG_ID = 0x00000001;
    public static final int FLAG_SPEC = 0x00000002;
    public static final int FLAG_AREA = 0x00000004;

    public static final int KEY_POWER = 1;
    public static final int KEY_ENGINE = 2;
    public static final int KEY_MOTOR = 3;
    public static final int KEY_GEAR = 4;
    public static final int KEY_HANDBRAKE = 5;
    public static final int KEY_CHARGE = 6;
    public static final int KEY_SPEED = 7;
    public static final int KEY_BATTERY_VOL = 8;
    public static final int KEY_BATTERY_PWR = 9;
    public static final int KEY_ASS = 10;
    public static final int KEY_DIAGNOSE = 11;
    public static final int KEY_TEL_DIAGNOSE = 12;
    public static final int KEY_VEHICLE_MODE = 13;
    public static final int KEY_LOCK = 14;
    public static final int KEY_WINDOW = 15;
    public static final int KEY_SECURITY = 16;
    public static final int KEY_HV_READY = 17;
    public static final int KEY_VTOL = 18;
    public static final int KEY_BATTERY_LEVEL = 19;
    public static final int KEY_PET_MODE = 20;
    public static final int KEY_SENTINEL_MODE = 21;

    public static final int KEY_DCDC_MODE = 22;

    public static final int KEY_OTA_MODE = 23;

    public static final int[] KEY_LIST = new int[] {
            KEY_POWER,
            KEY_ENGINE,
            KEY_MOTOR,
            KEY_GEAR,
            KEY_HANDBRAKE,
            KEY_CHARGE,
            KEY_SPEED,
            KEY_BATTERY_VOL,
            KEY_BATTERY_PWR,
            KEY_ASS,
            KEY_DIAGNOSE,
            KEY_TEL_DIAGNOSE,
            KEY_VEHICLE_MODE,
            KEY_LOCK,
            KEY_WINDOW,
            KEY_SECURITY,
            KEY_HV_READY,
            KEY_VTOL,
            KEY_BATTERY_LEVEL,
            KEY_PET_MODE,
            KEY_SENTINEL_MODE,
            KEY_DCDC_MODE,
            KEY_OTA_MODE
    };


    private static class VehicleServiceBinder extends IVehicleService.Stub {

        private IPropertyHandler mPropHandler = null;
        private IConditionHandler mCdtHandler = null;

        public void setPropHandler(IPropertyHandler handler) {
            mPropHandler = handler;
        }

        public void setConditionHandler(IConditionHandler handler) {
            mCdtHandler = handler;
        }

        @Override
        public Bundle readProperty(int flag) throws RemoteException {
            if(null == mPropHandler) {
                return null;
            }
            Bundle ret = new Bundle();

            if ((flag & FLAG_ID) > 0) {
                ret.putString("vin", mPropHandler.getVinCode());
            }

            if ((flag & FLAG_SPEC) > 0) {
                ret.putString("brand", mPropHandler.getBrand());
                ret.putString("model", mPropHandler.getModel());
            }

            Map<String, String> extra = mPropHandler.getExtra(flag);
            if(null != extra) {
                for (Map.Entry<String, String> entry : extra.entrySet()) {
                    if (!extra.containsKey(entry.getKey())) {
                        ret.putString(entry.getKey(), entry.getValue());
                    }
                }
            }
            return ret;
        }

        @Override
       public int queryCondition(int id, int def) throws RemoteException {
            if(null != mCdtHandler) {
                switch (id) {
                    case KEY_POWER:
                        return mCdtHandler.getPowerState();
                    case KEY_ENGINE:
                        return mCdtHandler.getEngineState();
                    case KEY_MOTOR:
                        return mCdtHandler.getMotorState();
                    case KEY_GEAR:
                        return mCdtHandler.getGearState();
                    case KEY_HANDBRAKE:
                        return mCdtHandler.getHandbrakeState();
                    case KEY_CHARGE:
                        return mCdtHandler.getChargeState();
                    case KEY_SPEED:
                        return mCdtHandler.getSpeed();
                    case KEY_BATTERY_VOL:
                        return mCdtHandler.getBatteryVoltage();
                    case KEY_BATTERY_PWR:
                        return mCdtHandler.getBatteryPower();
                    case KEY_DIAGNOSE:
                        return mCdtHandler.getDiagnoseState();
                    case KEY_TEL_DIAGNOSE:
                        return mCdtHandler.getTelDiagnoseState();
                    case KEY_VEHICLE_MODE:
                        return mCdtHandler.getVehicleModeState();
                    case KEY_LOCK:
                        return mCdtHandler.getLockState();
                    case KEY_WINDOW:
                        return mCdtHandler.getWindowState();
                    case KEY_SECURITY:
                        return mCdtHandler.getSecurityState();
                    case KEY_HV_READY:
                        return mCdtHandler.getHvReadyState();
                    case KEY_VTOL:
                        return mCdtHandler.getVtolState();
                    case KEY_BATTERY_LEVEL:
                        return mCdtHandler.getBatteryLevel();
                    case KEY_PET_MODE:
                        return mCdtHandler.getPetMode();
                    case KEY_SENTINEL_MODE:
                        return mCdtHandler.getSentinelMode();
                    case KEY_DCDC_MODE:
                        return mCdtHandler.getDcdcMode();
                    case KEY_OTA_MODE:
                        return mCdtHandler.getOtaMode();
                }
            }
            return def;
       }
    }

    private static VehicleServiceBinder sBinder = new VehicleServiceBinder();
    @Override
    public void onCreate() {
        super.onCreate();
        Context ctx = getApplicationContext();
        sBinder.setConditionHandler(onCreateConditionHandler(ctx));
        sBinder.setPropHandler(onCreatePropHandler(ctx));

    }

    @Override
    final public IBinder onBind(Intent intent) {
        return sBinder;
    }

    protected abstract IPropertyHandler onCreatePropHandler(Context context);

    protected abstract IConditionHandler onCreateConditionHandler(Context context);
}
