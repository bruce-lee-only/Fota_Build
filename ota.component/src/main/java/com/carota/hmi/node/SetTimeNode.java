package com.carota.hmi.node;

import static com.carota.cmh.CmhUtil.GROUP_SCHEDULE;
import static com.carota.core.ScheduleAttribute.TYPE_NORMAL;
import static com.carota.core.ScheduleAttribute.TYPE_SILENCE;

import com.carota.CarotaVehicle;
import com.carota.hmi.EventType;
import com.momock.util.Logger;

/**
 * Set Time
 */
class SetTimeNode extends BaseNode {
    private final long time;

    private String tid;

    SetTimeNode(StateMachine status, long time, String tid) {
        super(status);
        this.time = time;
        this.tid = tid;
    }

    @Override
    void onStart() {

    }

    @Override
    void onStop(boolean success) {

    }

    @Override
    public EventType getType() {
        return EventType.SET_TIME;
    }

    @Override
    public boolean execute() {
        try {
            return CarotaVehicle.setScheduleUpgrade(time, tid, TYPE_NORMAL) && CarotaVehicle.getSchedule().scheduleTime == time;
        } catch (Exception e) {
            Logger.error(e);
        }
        return false;
    }

}
