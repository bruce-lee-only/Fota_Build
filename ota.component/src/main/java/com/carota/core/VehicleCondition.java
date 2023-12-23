/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.core;

import com.carota.mda.remote.info.IVehicleStatus;
import com.carota.vehicle.IConditionHandler;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VehicleCondition implements IVehicleStatus {

    private static final String GEAR_PARKING = "P";
    private static final String GEAR_NEUTRAL = "N";
    private static final String GEAR_DRIVING = "D";
    private static final String GEAR_REVERSE = "R";

    private IVehicleStatus mStatus;

    public VehicleCondition(IVehicleStatus v) {
        mStatus = v;
    }

    public int getPowerState() {
        return mStatus.getPowerState();
    }

    @Override
    public int getGearState() {
        return mStatus.getGearState();
    }

    @Override
    public int getChargeState() {
        return mStatus.getChargeState();
    }

    @Override
    public int getSpeed() {
        return mStatus.getSpeed();
    }

    @Override
    public int getBatteryVoltage() {
        return mStatus.getBatteryVoltage();
    }

    @Override
    public int getBatteryPower() {
        return mStatus.getBatteryPower();
    }

    @Override
    public int getBatteryLevel() {
        return mStatus.getBatteryLevel();
    }

    @Override
    public int isPowerReady() {
        return mStatus.isPowerReady();
    }

    @Override
    public int isHandbrakeOn() {
        return mStatus.isHandbrakeOn();
    }

    @Override
    public int getDiagnoseState() {
        return mStatus.getDiagnoseState();
    }

    @Override
    public int getTelDiagnoseState() {
        return mStatus.getTelDiagnoseState();
    }

    @Override
    public int getVehicleModeState() {
        return mStatus.getVehicleModeState();
    }

    @Override
    public int getLockState() {
        return mStatus.getLockState();
    }

    @Override
    public int getWindowState() {
        return mStatus.getWindowState();
    }

    @Override
    public int getSecurityState() {
        return mStatus.getSecurityState();
    }

    @Override
    public int getHvReadyState() {
        return mStatus.getHvReadyState();
    }
    @Override
    public int getVtolState() {
        return mStatus.getVtolState();
    }

	@Override
    public int getPetMode() {
        return mStatus.getPetMode();
    }	
	
	@Override
    public int isMotor() {
        return mStatus.isMotor();
	}
	
    @Override
    public int getSentinelMode() {
        return mStatus.getSentinelMode();
    }

    @Override
    public String toString() {
        return mStatus.toString();
    }

    public boolean meet(Precondition cdt) {
        return meet(cdt, false);
    }

    public boolean meet(Precondition cdt, boolean ignoreUnknown) {
        // WARN: Here is a trick to convert IConditionHandler value to comparable value
        int target = 0;
        int temp = 0;
        switch (cdt.ID) {
            case POWER:
                target = mStatus.getPowerState();
                break;
            case ENGINE:
                target = mStatus.isPowerReady();
                break;
            case MOTOR:
                target = mStatus.isMotor();
                break;
            case GEAR:
                switch (mStatus.getGearState()) {
                    case STATE_GEAR_P:
                        target = GEAR_PARKING.hashCode();
                        break;
                    case STATE_GEAR_N:
                        target = GEAR_NEUTRAL.hashCode();
                        break;
                    case STATE_GEAR_D:
                        target = GEAR_DRIVING.hashCode();
                        break;
                    case STATE_GEAR_R:
                        target = GEAR_REVERSE.hashCode();
                        break;
                }
                break;
            case HANDBRAKE:
                target = mStatus.isHandbrakeOn();
                break;
            case CHARGING:
                target = mStatus.getChargeState();
                break;
            case SPEED:
                // m/h
                temp = mStatus.getSpeed();
                target = temp >= 0 ? temp * 3600 : IConditionHandler.STATE_UNKNOWN;
                break;
            case BATTERY_VOLTAGE:
                // mv
                target = mStatus.getBatteryVoltage();
                break;
            case BATTERY_POWER:
                // 0-100 %
                target = mStatus.getBatteryPower();
                break;
            case BATTERY_LEVEL:
                target = mStatus.getBatteryLevel();
                break;
            case DIAGNOSE:
                target = mStatus.getDiagnoseState();
                break;
            case TELDIAGONSE:
                target = mStatus.getTelDiagnoseState();
                break;
            case VEHICLEMODE:
                target = mStatus.getVehicleModeState();
                break;
            case LOCK:
                target = mStatus.getLockState();
                break;
            case WINDOW:
                target = mStatus.getWindowState();
                break;
            case SECURITY:
                target = mStatus.getSecurityState();
                break;
            case HV_READY:
                target = mStatus.getHvReadyState();
                break;
            case VTOL:
                target = mStatus.getVtolState();
                break;
            case CHERY_BATTERY:
                target = mStatus.getBatteryPower();
                break;
            case PET:
                target = mStatus.getPetMode();
                break;
            case SENTINEL:
                target = mStatus.getSentinelMode();
                break;
            default:
                return false;
        }
        if(IConditionHandler.STATE_UNKNOWN == target) {
            return ignoreUnknown;
        }
        return cdt.test(target);
    }

    public enum Item {
        POWER,
        ENGINE,
        MOTOR,
        GEAR,
        HANDBRAKE,
        CHARGING,
        ASS,

        SPEED,
        BATTERY_VOLTAGE,
        BATTERY_POWER,
        BATTERY_LEVEL,
        CHERY_BATTERY,
        DIAGNOSE,
        TELDIAGONSE,
        VEHICLEMODE,
        LOCK,
        WINDOW,
        SECURITY,
        HV_READY,
        VTOL,
        PET,
        SENTINEL
    }

    public static List<Precondition> parseFromSession(ISession s) {
        List<Precondition> ret = new ArrayList<>();
        for (String order : s.getCondition()) {
            Precondition cdt = Precondition.parse(order);
            if (null != cdt) {
                ret.add(cdt);
            }
        }
        return ret;
    }

    public static class Precondition {
        public final static char OPERATOR_EQUAL = '_';
        public final static char OPERATOR_GREATER = '>';
        public final static char OPERATOR_LESS = '<';

        public final Item ID;
        private String[] mOrder;
        private int mFlag;
        private int mValue;

        //Speed<3: 车速为0
        //Gear_P: 挡位为P
        //Gear_N: 挡位为N
        //Engine_0: 引擎处于停止状态
        //Ass_0:自动启停关闭
        //Handbrake_1: 手刹处于拉起状态
        //Ready_0: 车辆未处于Ready状态
        //Charge_0: 车辆未处于充电状态
        //Acc_0: 电源关闭
        //Acc_1: 电源处于ACC状态
        //Acc_2: 电源处于ON
        //Acc_3: 电源处于START
        //Battery>11: 电池大于11V
        //Battery>12: 电池大于12V
        //Battery>13: 电池大于13V
        //BatteryPower>80: 动力电池电池电量大于80% & 奇瑞蓄电池电量
        //BatteryLevel>80: 蓄电池电量大于80%
        //CheryBattery>80: chery动力电池电量大于80%
        //Diagnose_0: OBD未连接
        //Diagnose_1: OBD已经连接
        //Mode_0:vehicle factory
        //Mode_1: vehicle transport
        //Mode_2: normal
        //Mode_3: vehicle unrecognized
        //HvReady_0: off
        //HvReady_1 : on
        //Vtol_0 : off
        //Vtol_1 : on

        public Precondition(Item id, int shift, String... order) {
            ID = id;
            mOrder = order;
            mFlag = getFlag(order[1].charAt(0));
            mValue = parseValue(order[2], shift);
        }

        protected boolean test(int target) {
            int sign = mFlag ^ (mValue - target);
            if (0 == mFlag) {
                // operator is equal
                return 0 == sign;
            } else {
                return sign < 0;
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (String data : mOrder) {
                builder.append(data);
            }
            return builder.toString();
        }

        private int getFlag(char op) {
            if (OPERATOR_GREATER == op) {
                return 1;
            } else if (OPERATOR_LESS == op) {
                return -1;
            }
            return 0;
        }

        private int parseValue(String raw, int shift) {
            try {
                return (int) (Double.parseDouble(raw) * Math.pow(10, shift));
            } catch (NumberFormatException nfe) {
                return raw.hashCode();
            }
        }


        static Pattern pattern = Pattern.compile("([A-Za-z0-9]+)([_<>]+)([A-Za-z0-9.0-9]+)");

        public static Precondition parse(String order) {
            Matcher matcher = pattern.matcher(order);
            if (matcher.find()) {
                String key = matcher.group(1);
                Item type = null;
                int shift = 0;
                switch (key) {
                    case "Acc":
                        type = Item.POWER;
                        break;
                    case "Engine":
                        type = Item.ENGINE;
                        break;
                    case "Ready":
                        type = Item.MOTOR;
                        break;
                    case "Gear":
                        type = Item.GEAR;
                        break;
                    case "Handbrake":
                        type = Item.HANDBRAKE;
                        break;
                    case "Charge":
                        type = Item.CHARGING;
                        break;
                    case "Ass":
                        type = Item.ASS;
                        break;
                    case "Speed":
                        type = Item.SPEED;
                        shift = 3;
                        break;
                    case "Battery":
                        type = Item.BATTERY_VOLTAGE;
                        shift = 3;
                        break;
                    case "CheryBattery":
                        type = Item.BATTERY_POWER;
                        break;
                    case "BatteryPower":
                    case "BatteryLevel":
                        type = Item.BATTERY_LEVEL;
                        break;
                    case "Diagnose":
                        type = Item.DIAGNOSE;
                        break;
                    case "RemoteDiagnostic":
                        type = Item.TELDIAGONSE;
                        break;
                    case "Mode":
                        type = Item.VEHICLEMODE;
                        break;
                    case "Lock":
                        type = Item.LOCK;
                        break;
                    case "Window":
                        type = Item.WINDOW;
                        break;
                    case "Security":
                        type = Item.SECURITY;
                        break;
                    case "HvReady":
                        type = Item.HV_READY;
                        break;
                    case "Vtol":
                        type = Item.VTOL;
                        break;
                    case "PetMode":
                        type = Item.PET;
                        break;
                    case "SentinelMode":
                        type = Item.SENTINEL;
                        break;
                }
                if (null != type) {
                    return new Precondition(type, shift, matcher.group(1), matcher.group(2), matcher.group(3));
                }
            }
            return null;
        }
        public int getValue() {
            return mValue;
        }
    }
}
