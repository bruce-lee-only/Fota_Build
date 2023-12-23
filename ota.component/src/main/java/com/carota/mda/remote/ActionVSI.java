/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.mda.remote;

import android.os.Bundle;
import android.text.TextUtils;

import com.carota.core.VehicleEvent;
import com.carota.mda.remote.info.IVehicleStatus;
import com.carota.core.SystemAttribute;
import com.carota.mda.remote.info.VehicleDesc;
import com.carota.protobuf.Telemetry;
import com.carota.protobuf.VehicleStatusInformation;
import com.carota.svr.PrivReqHelper;
import com.carota.svr.PrivStatusCode;
import com.carota.util.LogUtil;
import com.carota.util.ReqTag;
import com.carota.vsi.VehicleInformation;
import com.google.protobuf.Internal;
import com.google.protobuf.InvalidProtocolBufferException;
import com.momock.util.Logger;

import java.util.List;

/**
 * Vehicle Status Information
 */

public class ActionVSI implements IActionVSI {

    private String mHost;

    public ActionVSI(String host) {
        mHost = host;
    }

    public VehicleDesc queryInfo() {
        final String CALL_TAG = LogUtil.TAG_RPC_VSI + "[INFO] ";
        Logger.info(CALL_TAG);
        Telemetry.EmptyReq.Builder req = Telemetry.EmptyReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA);

        PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + mHost + "/info", req.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                VehicleStatusInformation.VehicleInfoRsp rsp = VehicleStatusInformation.VehicleInfoRsp.parseFrom(resp.getBody());
                return new VehicleDesc(rsp);

            } catch (InvalidProtocolBufferException e) {
                Logger.error(e);
            }
        }
        return null;
    }

    @Override
    public IVehicleStatus queryStatus() {
        final String CALL_TAG = LogUtil.TAG_RPC_VSI + "[QUERY] ";
        Logger.info(CALL_TAG);
        Telemetry.EmptyReq.Builder req = Telemetry.EmptyReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA);
        PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + mHost + "/cdt", req.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (200 == resp.getStatusCode()) {
            try {
                VehicleStatusInformation.VehicleConditionRsp rsp = VehicleStatusInformation.VehicleConditionRsp.parseFrom(resp.getBody());
                return new VehicleStatus(rsp);
            } catch (Exception e) {
                Logger.error(e);
            }
        }
        return null;
    }

    public SystemAttribute setSystemAttribute(List<SystemAttribute.Configure> cfg) {
        final String CALL_TAG = LogUtil.TAG_RPC_VSI + "[SYS] ";
        Logger.info(CALL_TAG);
        VehicleStatusInformation.VehicleSysReq.Builder req = VehicleStatusInformation.VehicleSysReq.newBuilder();
        req.setTag(ReqTag.TAG_SRC_MDA);
        if(null != cfg) {
            for (SystemAttribute.Configure c : cfg) {
                switch (c.type) {
                    case TASK:
                        req.addData(VehicleStatusInformation.VehicleSysReq.Configure.newBuilder()
                                .setKey(VehicleStatusInformation.VehicleSysReq.Configure.Key.OTA_TASK)
                                .setValStr(c.get()).build());
                        break;
                    case STATE:
                        req.addData(VehicleStatusInformation.VehicleSysReq.Configure.newBuilder()
                                .setKey(VehicleStatusInformation.VehicleSysReq.Configure.Key.OTA_STATE)
                                .setValInt(c.get(0)).build());
                        break;
                    case NOTIFY:
                        req.addData(VehicleStatusInformation.VehicleSysReq.Configure.newBuilder()
                                .setKey(VehicleStatusInformation.VehicleSysReq.Configure.Key.TIMER_NOTIFY)
                                .setValStr(c.get()).build());
                        break;
                    case PROGRESS:
                        req.addData(VehicleStatusInformation.VehicleSysReq.Configure.newBuilder()
                                .setKey(VehicleStatusInformation.VehicleSysReq.Configure.Key.OTA_PROGRESS)
                                .setValInt(c.get(0)).build());
                        break;
                    case EXTRA:
                        req.addData(VehicleStatusInformation.VehicleSysReq.Configure.newBuilder()
                                .setKey(VehicleStatusInformation.VehicleSysReq.Configure.Key.OTA_EXTRA)
                                .setValStr(c.get()).build());
                        break;
                    case VIN:
                        req.addData(VehicleStatusInformation.VehicleSysReq.Configure.newBuilder()
                                .setKey(VehicleStatusInformation.VehicleSysReq.Configure.Key.OTA_VIN)
                                .setValStr(c.get()).build());
                        break;
                    case USID:
                        req.addData(VehicleStatusInformation.VehicleSysReq.Configure.newBuilder()
                                .setKey(VehicleStatusInformation.VehicleSysReq.Configure.Key.OTA_USID)
                                .setValStr(c.get()).build());
                        break;
                }
            }
        }
        PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + mHost + "/sys", req.build().toByteArray());
        Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
        if (PrivStatusCode.OK.equals(resp.getStatusCode())) {
            try {
                VehicleStatusInformation.VehicleSysRsp rsp = VehicleStatusInformation.VehicleSysRsp.parseFrom(resp.getBody());
                SystemAttribute attr = new SystemAttribute();
                attr.systemClock = rsp.getSystemClock();
                attr.systemTimer = rsp.getSystemTimer();
                attr.otaProgress = rsp.getOtaProgress();
                attr.otaState = rsp.getOtaState();
                attr.otaTask = rsp.getOtaTask();
                attr.modeOTA = parseModeState(rsp.getModeOta());
                attr.modePWR = parseModeState(rsp.getModePwr());
                return attr;
            } catch (InvalidProtocolBufferException e) {
                Logger.error(e);
            }
        }
        return null;
    }

    private int parseModeState(VehicleStatusInformation.VehicleSysRsp.ModeState ms) {
        switch (ms) {
            case MS_ON:
                return SystemAttribute.MODE_ON;
            case MS_OFF:
                return SystemAttribute.MODE_OFF;
            case MS_PHASE:
                return SystemAttribute.MODE_PHASE;
            default:
                return SystemAttribute.MODE_UNKNOWN;
        }
    }

    @Override
    public int registerEvent(String action, String activeUri) {
        if(!TextUtils.isEmpty(action)) {
            // TODO: chose action
            VehicleStatusInformation.EventReq.Builder req = VehicleStatusInformation.EventReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_MDA)
                    .setAction(VehicleStatusInformation.EventReq.Action.REGISTER)
                    .setEvent(VehicleStatusInformation.EventReq.Event.SCHEDULE)
                    .addData(activeUri == null ? "" : activeUri);
            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + mHost + "/event", req.build().toByteArray());
            if(PrivStatusCode.OK.equals(resp.getStatusCode())) {
                return 0;
            } else if(PrivStatusCode.READY.equals(resp.getStatusCode())) {
                return 1;
            }
        }
        return -1;
    }

    @Override
    public boolean cleanEvent(String action, long delaySec, Bundle extra) {
        final String CALL_TAG = LogUtil.TAG_RPC_VSI + "[FIRE] ";
        Logger.info(CALL_TAG + action + " " + delaySec);
        VehicleStatusInformation.EventReq.Builder req = VehicleStatusInformation.EventReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA)
                .setAction(VehicleStatusInformation.EventReq.Action.FIRE)
                .setDelay(delaySec);
        if (extra != null) {
            req.addData(extra.getString("action", ""));
            if (extra.getBoolean("dtc", false)) req.addData("dtc");
        }

        if (!TextUtils.isEmpty(action) && delaySec >= 0) {
            switch (action) {
                case VehicleEvent.EVENT_POWER_OFF:
                    req.setEvent(VehicleStatusInformation.EventReq.Event.ACC_OFF);
                    break;
                default:
                    return false;
            }
            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + "ota_tbox_vsi" + "/event", req.build().toByteArray());
            Logger.info(CALL_TAG + "cleanEvent RSP : %1d", resp.getStatusCode());

            return PrivStatusCode.OK.equals(resp.getStatusCode());
        }
        return false;
    }

    @Override
    public boolean fireEvent(String action, long delaySec, Bundle extra) {
        final String CALL_TAG = LogUtil.TAG_RPC_VSI + "[FIRE] ";
        Logger.info(CALL_TAG + action + " " + delaySec);
        VehicleStatusInformation.EventReq.Builder req = VehicleStatusInformation.EventReq.newBuilder()
                .setTag(ReqTag.TAG_SRC_MDA)
                .setAction(VehicleStatusInformation.EventReq.Action.FIRE)
                .setDelay(delaySec);
        if (extra != null) {
            req.addData(extra.getString("action", ""));
            if (extra.getBoolean("dtc", false)) req.addData("dtc");
        }

        if (!TextUtils.isEmpty(action) && delaySec >= 0) {
            switch (action) {
                case VehicleEvent.EVENT_POWER_OFF:
                    req.setEvent(VehicleStatusInformation.EventReq.Event.ACC_OFF);
                    break;
                case VehicleEvent.EVENT_POWER_ON:
                    req.setEvent(VehicleStatusInformation.EventReq.Event.ACC_ON);
                    break;
                case VehicleEvent.EVENT_SCHEDULE:
                    req.setEvent(VehicleStatusInformation.EventReq.Event.SCHEDULE);
                    break;
                case VehicleEvent.EVENT_RUNTIME:
                    req.setEvent(VehicleStatusInformation.EventReq.Event.MODE_OTA);
                    break;
                case VehicleEvent.EVENT_ELECTRIC_RUNTIME:
                    req.setEvent(VehicleStatusInformation.EventReq.Event.MODE_PWR_OTA);
                    break;
                default:
                    return false;
            }
            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + mHost + "/event", req.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());

            return PrivStatusCode.OK.equals(resp.getStatusCode());
        }
        return false;
    }

    @Override
    public boolean removeEvent(String action) {
        final String CALL_TAG = LogUtil.TAG_RPC_VSI + "[REMOVE] ";
        Logger.info(CALL_TAG);
        if (!TextUtils.isEmpty(action)) {
            // TODO: chose action
            VehicleStatusInformation.EventReq.Builder req = VehicleStatusInformation.EventReq.newBuilder()
                    .setTag(ReqTag.TAG_SRC_MDA)
                    .setAction(VehicleStatusInformation.EventReq.Action.REMOVE)
                    .setEvent(VehicleStatusInformation.EventReq.Event.SCHEDULE);

            PrivReqHelper.Response resp = PrivReqHelper.doPost("http://" + mHost + "/event", req.build().toByteArray());
            Logger.info(CALL_TAG + "RSP : %1d", resp.getStatusCode());
            return PrivStatusCode.OK.equals(resp.getStatusCode());
        }
        return false;
    }

    private static class VehicleStatus implements IVehicleStatus{

        private VehicleStatusInformation.VehicleConditionRsp mRaw;

        public VehicleStatus(VehicleStatusInformation.VehicleConditionRsp info) {
            mRaw = info;
        }

        public int getPowerState() {
            return convert(mRaw.getPower());
        }

        public int getGearState() {
            return convert(mRaw.getGear());
        }

        public int getChargeState() {
            return convert(mRaw.getCharging());
        }

        public int getSpeed() {
            return mRaw.getSpeed();
        }

        public int getBatteryVoltage() {
            return mRaw.getBatteryVoltage();
        }

        public int getBatteryPower() {
            return mRaw.getBatteryPower();
        }

        @Override
        public int getBatteryLevel() {
            return mRaw.getBatteryLevel();
        }

        public int isPowerReady() {
            return convert(mRaw.getPowerReady());
        }

        public int isHandbrakeOn() {
            return convert(mRaw.getHandbrake());
        }

        public int getDiagnoseState() {
            return convert(mRaw.getDiagnose());
        }

        @Override
        public int getTelDiagnoseState() {
            return convert(mRaw.getTelDiagnose());
        }

        @Override
        public int getVehicleModeState() {
            return convert(mRaw.getVehicleMode());
        }

        @Override
        public int getLockState() {
            return convert(mRaw.getLock());
        }

        @Override
        public int getWindowState() {
            return convert(mRaw.getWindow());
        }

        @Override
        public int getSecurityState() {
            return convert(mRaw.getSecurity());
        }


        @Override
        public int getHvReadyState() {
            return convert(mRaw.getHvReady());
        }

        @Override
        public int getVtolState() {
            return convert(mRaw.getVtol());
        }


		@Override
        public int isMotor() {
            return convert(mRaw.getMotor());
        }
		
		@Override
        public int getPetMode() {
            return convert(mRaw.getPetMode());
        }
		
		@Override
        public int getSentinelMode() {
            return convert(mRaw.getSentinelMode());
        }

        private int convert(Internal.EnumLite e) {
            // WARN: Here is a trick to convert PB enum to IConditionHandler value
            return e.getNumber() - 1;
        }

        @Override
        public String toString() {
//            return mRaw.toString();
            return "PowerState = " + mRaw.getPower()
                    + "; GearState = " + mRaw.getGear()
                    + "; ChargeState = " + mRaw.getCharging()
                    + "; Speed = " + mRaw.getSpeed()
                    + "; BatteryVoltage = " + mRaw.getBatteryVoltage()
                    + "; BatteryPower = " + mRaw.getBatteryPower()
                    + "; BatterLevel = " + mRaw.getBatteryLevel()
                    + "; Handbrake = " + mRaw.getHandbrake()
                    + "; PowerReady = " + mRaw.getPowerReady()
                    + "; Diagnose = " + mRaw.getDiagnose()
                    + "; TelDiagnose = " + mRaw.getTelDiagnose()
                    + "; VehicleMode = " + mRaw.getVehicleMode()
                    + "; Lock = " + mRaw.getLock()
                    + "; Window = " + mRaw.getWindow()
                    + "; Security = " + mRaw.getSecurity()
                    + "; HvReady =" + mRaw.getHvReady()
                    + "; Vtol =" + mRaw.getVtol()
                    + "; BatteryLevel =" + mRaw.getBatteryLevel();
        }
    }
}
