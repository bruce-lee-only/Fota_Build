/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.vsi.provider;

import com.carota.protobuf.VehicleStatusInformation;
import com.carota.svr.HttpResp;
import com.carota.svr.PrivStatusCode;
import com.carota.svr.SimpleHandler;
import com.carota.vehicle.IConditionHandler;
import com.carota.vsi.VehicleInformation;
import com.momock.util.Logger;

import java.util.List;
import java.util.Map;

public class VehicleConditionHandler extends SimpleHandler {

    private VehicleInformation mService;

    public VehicleConditionHandler(VehicleInformation service) {
        super();
        mService = service;
    }

    @Override
    public HttpResp post(String path, Map<String, List<String>> params, byte[] body, Object extra) {
        PrivStatusCode code = PrivStatusCode.SRV_UNKNOWN;
        VehicleStatusInformation.VehicleConditionRsp.Builder builder = VehicleStatusInformation.VehicleConditionRsp.newBuilder();

        try {
             code = queryStatus(builder);
        } catch (Exception e) {
            Logger.error(e);
        }
        return HttpResp.newInstance(code, builder.build().toByteArray());
    }

    private PrivStatusCode queryStatus(VehicleStatusInformation.VehicleConditionRsp.Builder rsp) throws Exception {
        IConditionHandler cdt = mService.queryCondition();
        if(cdt == null ) return PrivStatusCode.REQ_TARGET_UNKNOWN;
        rsp.setBatteryPower(cdt.getBatteryPower())
                .setSpeed(cdt.getSpeed())
                .setBatteryVoltage(cdt.getBatteryVoltage())
                .setBatteryLevel(cdt.getBatteryLevel());

        int motorReady = cdt.getMotorState();
        int engineReady = cdt.getEngineState();
        if (IConditionHandler.STATE_ENGINE_ON == engineReady
                || IConditionHandler.STATE_MOTOR_READY == motorReady) {
            rsp.setPowerReady(VehicleStatusInformation.VehicleConditionRsp.Ready.READY_ON);
        } else if (IConditionHandler.STATE_ENGINE_OFF == engineReady
                || IConditionHandler.STATE_MOTOR_OFF == motorReady) {
            rsp.setPowerReady(VehicleStatusInformation.VehicleConditionRsp.Ready.READY_OFF);
        } else {
            rsp.setPowerReady(VehicleStatusInformation.VehicleConditionRsp.Ready.READY_UNKNOWN);
        }

        switch (cdt.getHandbrakeState()) {
            case IConditionHandler.STATE_HANDBRAKE_OFF:
                rsp.setHandbrake(VehicleStatusInformation.VehicleConditionRsp.Handbrake.HANDBRAKE_OFF);
                break;
            case IConditionHandler.STATE_HANDBRAKE_ON:
                rsp.setHandbrake(VehicleStatusInformation.VehicleConditionRsp.Handbrake.HANDBRAKE_ON);
                break;
        }
        
        switch (cdt.getChargeState()) {
            case IConditionHandler.STATE_CHARGE_OFF:
                rsp.setCharging(VehicleStatusInformation.VehicleConditionRsp.Charging.CHG_OFF);
                break;
            case IConditionHandler.STATE_CHARGE_IDLE:
                rsp.setCharging(VehicleStatusInformation.VehicleConditionRsp.Charging.CHG_IDLE);
                break;
            case IConditionHandler.STATE_CHARGE_SLOW:
                rsp.setCharging(VehicleStatusInformation.VehicleConditionRsp.Charging.CHG_SLOW);
                break;
            case IConditionHandler.STATE_CHARGE_FAST:
                rsp.setCharging(VehicleStatusInformation.VehicleConditionRsp.Charging.CHG_FAST);
                break;
        }

        switch (cdt.getPowerState()) {
            case IConditionHandler.STATE_POWER_OFF:
                rsp.setPower(VehicleStatusInformation.VehicleConditionRsp.Power.PWR_OFF);
                break;
            case IConditionHandler.STATE_POWER_ACC:
                rsp.setPower(VehicleStatusInformation.VehicleConditionRsp.Power.PWR_ACC);
                break;
            case IConditionHandler.STATE_POWER_ON:
                rsp.setPower(VehicleStatusInformation.VehicleConditionRsp.Power.PWR_ON);
                break;
            case IConditionHandler.STATE_POWER_START:
                rsp.setPower(VehicleStatusInformation.VehicleConditionRsp.Power.PWR_START);
                break;
        }

        switch (cdt.getGearState()){
            case IConditionHandler.STATE_GEAR_P:
                rsp.setGear(VehicleStatusInformation.VehicleConditionRsp.Gear.PARKING);
                break;
            case IConditionHandler.STATE_GEAR_N:
                rsp.setGear(VehicleStatusInformation.VehicleConditionRsp.Gear.NEUTRAL);
                break;
            case IConditionHandler.STATE_GEAR_D:
                rsp.setGear(VehicleStatusInformation.VehicleConditionRsp.Gear.DRIVE);
                break;
            case IConditionHandler.STATE_GEAR_R:
                rsp.setGear(VehicleStatusInformation.VehicleConditionRsp.Gear.REVERSE);
                break;
        }

        int diagnoseState = cdt.getDiagnoseState();
        if (IConditionHandler.STATE_DIAGNOSE_CONNECTED == diagnoseState) {
            rsp.setDiagnose(VehicleStatusInformation.VehicleConditionRsp.Diagnose.DIAGNOSE_CONNECTED);
        } else if (IConditionHandler.STATE_DIAGNOSE_OFF == diagnoseState) {
            rsp.setDiagnose(VehicleStatusInformation.VehicleConditionRsp.Diagnose.DIAGNOSE_OFF);
        }

        int telDiagnoseState = cdt.getTelDiagnoseState();
        if (IConditionHandler.STATE_TEL_DIAGNOSE_READ == telDiagnoseState) {
            rsp.setTelDiagnose(VehicleStatusInformation.VehicleConditionRsp.TelDiagnose.TEL_DIAGNOSE_READ);
        } else if (IConditionHandler.STATE_TEL_DIAGNOSE_NOT_READ == telDiagnoseState) {
            rsp.setTelDiagnose(VehicleStatusInformation.VehicleConditionRsp.TelDiagnose.TEL_DIAGNOSE_NOT_READ);
        }

        Logger.info("model :" + cdt.getVehicleModeState());
        switch (cdt.getVehicleModeState()) {
            case IConditionHandler.STATE_VEHICLE_FACTORY:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.FACTORY);
                break;
            case IConditionHandler.STATE_VEHICLE_TRANSPORT:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.TRANSPORT);
                break;
            case IConditionHandler.STATE_VEHICLE_NORMAL:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.NORMAL);
                break;
            case IConditionHandler.STATE_VEHICLE_UNRECOGNIZED:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.VEHICLE_UNRECOGNIZED);
                break;
            case IConditionHandler.STATE_VEHICLE_DYNO:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.DYNO);
                break;
            case IConditionHandler.STATE_VEHICLE_CRASH:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.CRASH);
                break;
            case IConditionHandler.STATE_VEHICLE_FACTORY_PAUSED:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.FACTORY_PAUSED);
                break;
            case IConditionHandler.STATE_VEHICLE_TRANSPORT_PAUSED:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.TRANSPORT_PAUSED);
                break;
            default:
                rsp.setVehicleMode(VehicleStatusInformation.VehicleConditionRsp.VehicleMode.VEHICLE_UNKNOWN);
                break;
        }

        int lockState = cdt.getLockState();
        if (IConditionHandler.STATE_LOCK_ON == lockState) {
            rsp.setLock(VehicleStatusInformation.VehicleConditionRsp.Lock.LOCK_ON);
        } else if (IConditionHandler.STATE_LOCK_OFF == lockState) {
            rsp.setLock(VehicleStatusInformation.VehicleConditionRsp.Lock.LOCK_OFF);
        }

        int windowState = cdt.getWindowState();
        if (IConditionHandler.STATE_WINDOW_OPENED == windowState) {
            rsp.setWindow(VehicleStatusInformation.VehicleConditionRsp.Window.WINDOW_OPENED);
        } else if (IConditionHandler.STATE_WINDOW_CLOSED == windowState) {
            rsp.setWindow(VehicleStatusInformation.VehicleConditionRsp.Window.WINDOW_CLOSED);
        }

        int securityState = cdt.getSecurityState();
        if (IConditionHandler.STATE_SECURITY_ON == securityState) {
            rsp.setSecurity(VehicleStatusInformation.VehicleConditionRsp.Security.SECURITY_ON);
        } else if (IConditionHandler.STATE_SECURITY_OFF == securityState) {
            rsp.setSecurity(VehicleStatusInformation.VehicleConditionRsp.Security.SECURITY_OFF);
        }

        switch (cdt.getHvReadyState()) {
            case IConditionHandler.STATE_HV_READY_OFF:
                rsp.setHvReady(VehicleStatusInformation.VehicleConditionRsp.HvReady.HV_READY_OFF);
                break;
            case IConditionHandler.STATE_HV_READY_ON:
                rsp.setHvReady(VehicleStatusInformation.VehicleConditionRsp.HvReady.HV_READY_ON);
                break;
            default:
                rsp.setHvReady(VehicleStatusInformation.VehicleConditionRsp.HvReady.HV_READY_UNKNOWN);
                break;

        }
        switch (cdt.getVtolState()) {
            case IConditionHandler.STATE_VTOL_OFF:
                rsp.setVtol(VehicleStatusInformation.VehicleConditionRsp.Vtol.VTOL_OFF);
                break;
            case IConditionHandler.STATE_VTOL_ON:
                rsp.setVtol(VehicleStatusInformation.VehicleConditionRsp.Vtol.VTOL_ON);
                break;
            default:
                rsp.setVtol(VehicleStatusInformation.VehicleConditionRsp.Vtol.VTOL_UNKNOWN);
                break;
        }

        switch (cdt.getPetMode()) {
            case IConditionHandler.PET_MODE_OFF:
                rsp.setPetMode(VehicleStatusInformation.VehicleConditionRsp.PetMode.PET_MODE_OFF);
                break;
            case IConditionHandler.PET_MODE_ON:
                rsp.setPetMode(VehicleStatusInformation.VehicleConditionRsp.PetMode.PET_MODE_ON);
                break;
            default:
                rsp.setPetMode(VehicleStatusInformation.VehicleConditionRsp.PetMode.PET_MODE_UNKNOWN);
                break;
        }

        switch (cdt.getSentinelMode()) {
            case IConditionHandler.SENTINEL_MODE_OFF:
                rsp.setSentinelMode(VehicleStatusInformation.VehicleConditionRsp.SentinelMode.SENTINEL_MODE_OFF);
                break;
            case IConditionHandler.SENTINEL_MODE_ON:
                rsp.setSentinelMode(VehicleStatusInformation.VehicleConditionRsp.SentinelMode.SENTINEL_MODE_ON);
                break;
            default:
                rsp.setSentinelMode(VehicleStatusInformation.VehicleConditionRsp.SentinelMode.SENTINEL_MODE_UNKNOWN);
                break;
        }

        switch (cdt.getDcdcMode()) {
            case IConditionHandler.DCDC_MODE_OFF:
                rsp.setDcdcMode(VehicleStatusInformation.VehicleConditionRsp.DcdcMode.DCDC_MODE_OFF);
                break;
            case IConditionHandler.DCDC_MODE_ON:
                rsp.setDcdcMode(VehicleStatusInformation.VehicleConditionRsp.DcdcMode.DCDC_MODE_ON);
                break;
            default:
                rsp.setDcdcMode(VehicleStatusInformation.VehicleConditionRsp.DcdcMode.DCDC_MODE_UNKNOWN);
                break;
        }

        switch (cdt.getOtaMode()) {
            case IConditionHandler.OTA_MODE_OFF:
                rsp.setOtaMode(VehicleStatusInformation.VehicleConditionRsp.OtaMode.OTA_MODE_OFF);
                break;
            case IConditionHandler.OTA_MODE_ON:
                rsp.setOtaMode(VehicleStatusInformation.VehicleConditionRsp.OtaMode.OTA_MODE_ON);
                break;
            default:
                rsp.setOtaMode(VehicleStatusInformation.VehicleConditionRsp.OtaMode.OTA_MODE_UNKNOWN);
                break;
        }
        return PrivStatusCode.OK;
    }
}
