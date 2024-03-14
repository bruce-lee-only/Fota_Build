/*
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 */
package com.carota.util;

import com.carota.CarotaClient;
import com.carota.core.VehicleCondition;
import com.carota.core.report.Event;
import com.carota.mda.remote.info.IVehicleStatus;
import com.momock.util.Logger;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import ai.carota.iov.ota.master.mqtt.protobuf.ErrorCode;

public class SendEventUtil {
    //进入OTA模式的前置条件
    public static final int PRE_TYPE_OTA = 0;
    //进入升级的前置条件
    public static final int PRE_TYPE_INSTALL = 1;

    private static final SerialExecutor sExecutor = new SerialExecutor();

    /**
     * 发送失败的前置条件到后台
     *
     * @param conditionList    失败的前置条件列表
     * @param vehicleCondition
     * @param type             类型 参考 PRE_TYPE_OTA，PRE_TYPE_INSTALL
     */
    public static void sendPreconditionEvent(List<VehicleCondition.Precondition> conditionList, VehicleCondition vehicleCondition, int type) {
        if (conditionList == null || conditionList.size() == 0) {
            Logger.error("sendPreconditionEvent error because conditionList");
            return;
        }
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                for (VehicleCondition.Precondition condition : conditionList) {
                    int code = 0;
                    HashMap<String, String> map = new HashMap<>();
                    switch (condition.ID) {
                        case SPEED:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_SPEED_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_SPEED_VALUE;
                            }
                            map.put("Speed", vehicleCondition.getSpeed() + "");
                            break;
                        case HANDBRAKE:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_HAND_BREAK_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_HAND_BREAK_VALUE;
                            }
                            map.put("HandBrake", vehicleCondition.isHandbrakeOn() + "");
                            break;
                        case GEAR:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_GEAR_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_GEAR_VALUE;
                            }
                            String gear = "-1";
                            switch (vehicleCondition.getGearState()) {
                                case IVehicleStatus.STATE_GEAR_P:
                                    gear = "P";
                                    break;
                                case IVehicleStatus.STATE_GEAR_N:
                                    gear = "N";
                                    break;
                                case IVehicleStatus.STATE_GEAR_D:
                                    gear = "D";
                                    break;
                                case IVehicleStatus.STATE_GEAR_R:
                                    gear = "R";
                                    break;
                            }
                            map.put("Gear", gear);
                            break;
                        case POWER:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_POWER_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_POWER_VALUE;
                            }
                            String acc = "-1";
                            switch (vehicleCondition.getPowerState()) {
                                case IVehicleStatus.STATE_POWER_ACC:
                                    acc = "Acc";
                                    break;
                                case IVehicleStatus.STATE_POWER_OFF:
                                    acc = "Off";
                                    break;
                                case IVehicleStatus.STATE_POWER_ON:
                                    acc = "On";
                                    break;
                                case IVehicleStatus.STATE_POWER_START:
                                    acc = "Start";
                                    break;
                            }
                            map.put("Acc", acc);
                            break;
                        case DIAGNOSE:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_DIAGNOSE_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_DIAGNOSE_VALUE;
                            }
                            map.put("Diagnose", vehicleCondition.getDiagnoseState() + "");
                            break;
                        case SECURITY:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_SECURITY_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_SECURITY_VALUE;
                            }
                            map.put("Security", vehicleCondition.getSecurityState() + "");
                            break;
                        case DCDC_MODE:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_DCDC_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_DCDC_VALUE;
                            }
                            map.put("DCDC", vehicleCondition.getDcdcMode() + "");
                            break;
                        case OTA_MODE:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_OTA_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_OTA_VALUE;
                            }
                            map.put("OTA", vehicleCondition.getOtaMode() + "");
                            break;
                        case BATTERY_POWER:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_BATTERY_POWER_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_BATTERY_POWER_VALUE;
                            }
                            map.put("BatteryPower", vehicleCondition.getBatteryPower() + "");
                            break;
                        case HV_READY:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_HV_READY_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_HV_READY_VALUE;
                            }
                            map.put("HvReady", vehicleCondition.getHvReadyState() + "");
                            break;
                        case CHARGING:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_CHARGING_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_PRE_CONDITION_CHARGING_VALUE;
                            }
                            map.put("HvReady", vehicleCondition.getChargeState() + "");
                            break;
                        default:
                            if (type == PRE_TYPE_OTA) {
                                code = ErrorCode.CodeType.HMI_OTA_CONDITION_UNKNOWN_VALUE;
                            } else if (type == PRE_TYPE_INSTALL) {
                                code = ErrorCode.CodeType.HMI_CDTSTATUSDETECTION_UNPASS_VALUE;
                            }
                            map.put("error", condition.ID + " -1");
                            break;
                    }
                    JSONObject root = new JSONObject(map);
                    CarotaClient.sendUiEvent(Event.UpgradeType.DEFAULT, code, root.toString(), Event.Result.RESULT_FAIL);
                }
            }
        });
    }
}
