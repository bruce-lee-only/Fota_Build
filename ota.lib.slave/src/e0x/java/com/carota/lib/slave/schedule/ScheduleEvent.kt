package com.carota.lib.slave.schedule

import com.carota.lib.common.uitls.Logger
import com.carota.lib.slave.ISchedule
import java.util.ArrayList
import java.util.Calendar

class ScheduleEvent: ISchedule {
    override fun loadScheduleEvent(): List<ISchedule.ScheduleEventInfo>? {
        try {
            Logger.info("ScheduleEvent load schedule event")
            val scheduleList: MutableList<ISchedule.ScheduleEventInfo> = ArrayList()
            val calendar = Calendar.getInstance()

            //fixme: need add change start time
            val chargeStart = 0L
            //fixme: need add change delay time
            val chargeDelay = 0L
            val chargingInfo: ISchedule.ScheduleEventInfo?
            if (chargeDelay > 0){
                chargingInfo = ISchedule.ScheduleEventInfo(ISchedule.CHARGING)
                calendar.timeInMillis = chargeStart
                chargingInfo.start = chargeStart
                chargingInfo.delay = chargeDelay
                chargingInfo.end = calendar.timeInMillis + chargeDelay
                scheduleList.add(chargingInfo)
            }

            //fixme: need add trip event load
        }catch (e: Exception){
            Logger.error("ScheduleEvent load schedule event exception: $e")
        }
        return null
    }
}