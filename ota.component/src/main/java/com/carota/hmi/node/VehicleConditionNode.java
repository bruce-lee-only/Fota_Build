package com.carota.hmi.node;

import android.util.Log;

import com.carota.CarotaVehicle;
import com.carota.core.VehicleCondition;
import com.carota.hmi.EventType;
import com.carota.hmi.ICallBack;
import com.carota.hmi.action.ConditionAction;
import com.carota.hmi.callback.ICondition;
import com.momock.util.Logger;

import java.util.ArrayList;
import java.util.List;

class VehicleConditionNode extends BaseNode {
    private VehicleCondition condition;
    private List<ICondition.IConditionItem> result;

    private ArrayList<VehicleCondition.Precondition> extraCondition = new ArrayList<>();

    VehicleConditionNode(StateMachine status) {
        super(status);
    }

    @Override
    void onStart() {
        mCallBack.condition().onStart();
    }

    @Override
    void onStop(boolean success) {
        mCallBack.condition().onStop(success, new ConditionAction(success, isAutoRunNextNode(), mHandler), result);
    }

    @Override
    public EventType getType() {
        return EventType.CONDITION;
    }

    @Override
    protected boolean execute() {
        result = new ArrayList<>();
        return vehicleCondition();
    }

    @Override
    public List<ICondition.IConditionItem> getVerifyResult(){
        return result;
    }

    @Override
    public void setExtraCondition(ArrayList<VehicleCondition.Precondition> extraCondition) {
        this.extraCondition = extraCondition;
    }

    private int getErrorCode(VehicleCondition.Precondition precondition) {
        switch (precondition.ID) {
            case POWER:
                return ICallBack.STATE_CONDITION_POWER_FAIL;
            case ENGINE:
                return ICallBack.STATE_CONDITION_ENGINE_FAIL;
            case MOTOR:
                return ICallBack.STATE_CONDITION_MOTOR_FAIL;
            case GEAR:
                return ICallBack.STATE_CONDITION_GEAR_FAIL;
            case HANDBRAKE:
                return ICallBack.STATE_CONDITION_HANDBRAKE_FAIL;
            case CHARGING:
                return ICallBack.STATE_CONDITION_CHARGING_FAIL;
            case ASS:
                return ICallBack.STATE_CONDITION_ASS_FAIL;
            case SPEED:
                return ICallBack.STATE_CONDITION_SPEED_FAIL;
            case BATTERY_VOLTAGE:
                return ICallBack.STATE_CONDITION_BATTERY_VOLTAGE_FAIL;
            case BATTERY_POWER:
                return ICallBack.STATE_CONDITION_BATTERY_POWER_FAIL;
            case DIAGNOSE:
                return ICallBack.STATE_CONDITION_DIAGNOSE_FAIL;
        }
        return 0;
    }

    @Override
    public VehicleCondition getVehicleCondition() {
        return condition;
    }

    private boolean vehicleCondition() {
        condition = CarotaVehicle.queryVehicleCondition();
        boolean isSuccess = true;
        if (condition != null) {

            preconditionLog(condition);
            ArrayList<VehicleCondition.Precondition> pre = new ArrayList<>();
            pre.addAll(VehicleCondition.parseFromSession(mStatus.getSession()));
            pre.addAll(extraCondition);

            for (VehicleCondition.Precondition c : pre) {
                boolean meet = condition.meet(c);
                result.add(new ICondition.IConditionItem(c.ID, meet));
                Logger.info("HMI Vehicle Condition %s is meet:%b @%s", c.ID, meet, getType());
                if (!condition.meet(c)) {
                    isSuccess = false;
                }
            }
        }else {
            isSuccess = false;
        }
        return isSuccess;
    }

    private void preconditionLog(VehicleCondition condition){
        Logger.debug("****************  Vehicle condition list  ****************");
        Logger.debug("BatteryLevel:" + condition.getBatteryLevel());
        Logger.debug("BatteryPower:" + condition.getBatteryPower());
        Logger.debug("Speed:" + condition.getSpeed());
        Logger.debug("BatteryVoltage:" + condition.getBatteryVoltage());
        Logger.debug("ChargeState:" + condition.getChargeState());
        Logger.debug("isHandbrakeOn:" + condition.isHandbrakeOn());
        Logger.debug("DiagnoseState:" + condition.getDiagnoseState());
        Logger.debug("WindowState:" + condition.getWindowState());
        Logger.debug("SecurityState:" + condition.getSecurityState());
        Logger.debug("LockState:" + condition.getLockState());
        Logger.debug("VehicleMode:" + condition.getVehicleModeState());
        Logger.debug("TelDiagnose:" + condition.getTelDiagnoseState());
        Logger.debug("Gear:" + condition.getGearState());
        Logger.debug("Ready:" + condition.isMotor());
        Logger.debug("Pet:" + condition.getPetMode());
        Logger.debug("****************  Vehicle condition list  ****************");
    }
}
