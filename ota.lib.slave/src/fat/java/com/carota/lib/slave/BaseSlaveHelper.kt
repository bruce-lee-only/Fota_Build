package com.carota.lib.slave

import android.os.Bundle

abstract class BaseSlaveHelper {

    val delayTime = 15 * 60 * 1000

    abstract fun hideRedPoint()

    abstract fun displayRedPoint()

    abstract fun checkScheduleEvent(startTime: Long, endTime: Long = startTime + delayTime): Bundle?
}