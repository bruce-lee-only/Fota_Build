package com.carota.lib.slave

interface ISchedule {

    fun loadScheduleEvent():List<ScheduleEventInfo>?

    data class ScheduleEventInfo(val name: String){
        var start   : Long  = 0     //ms
        var end     : Long  = 0     //ms
        var delay   : Long  = 0     //ms
    }

    companion object{
        const val CHARGING  = "charging"
        const val TRIP      = "trip"
    }
}