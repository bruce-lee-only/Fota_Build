package com.carota.hmi.node;

import android.os.Handler;

import com.carota.CarotaVehicle;
import com.carota.hmi.EventType;
import com.carota.hmi.callback.CallBackManager;
import com.carota.hmi.callback.ICallBack;
import com.carota.hmi.status.HmiStatus;
import com.momock.util.Logger;

/**
 * Set Time
 */
class SetTimeNode extends BaseNode {
    private final long time;

    SetTimeNode(HmiStatus hmiStatus, Handler handler, CallBackManager callback, long time) {
        super(hmiStatus, handler, callback);
        this.time = time;
    }

    @Override
    public EventType getType() {
        return EventType.SET_TIME;
    }

    @Override
    public boolean execute() {
        try {
            return CarotaVehicle.setScheduleUpgrade(time) && CarotaVehicle.getSchedule().scheduleTime == time;
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

}
