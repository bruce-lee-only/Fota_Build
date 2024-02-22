package com.carota.lib.slave

import android.os.Bundle
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SlaveHandler{

    companion object: KoinComponent{
        private val helper: SlaveHelper by inject()

        /**
         * @param startTime Long: system current time + server delay time
         * @param endTime Long: when endTime<=0 use default delay time
         */
        private fun checkScheduleEvent(startTime: Long, endTime: Long): Bundle?{
            return if (endTime <= 0)
                helper.checkScheduleEvent(startTime)
            else
                helper.checkScheduleEvent(startTime, endTime)
        }

        fun isScheduleEventPass(startTime: Long, endTime: Long): Boolean {
             checkScheduleEvent(startTime, endTime)?.let { return true } ?: return false
        }
    }
}