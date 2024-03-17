package com.carota.lib.status.shared

import android.content.Context

val sharedApp by lazy { SharedApp() }
class SharedManager {

    companion object Statical{
        const val TAG_REBOOT_DISPLAY            = "reboot_display"
        const val TAG_VEHICLE_MODE              = "vehicle_mode"
        const val TAG_SCHEDULE_TIME             = "schedule_time"
        const val TAG_UPGRADE_STATUS            = "upgrade_status"

        const val DEF_IS_DEBUG_MODE: Boolean    = false
        const val DEF_IS_ENGINE_MODE: Boolean   = false
        const val DEF_REBOOT_DISPLAY: String    = ""
        const val DEF_SCHEDULE_TIME: Long       = -1L
        const val DEF_UPGRADE_STATUS: String    = "never"

        const val VEHICLE_MODE_REAL             = "real"
        const val VEHICLE_MODE_VIRTUAL          = "virtual"
        const val DEF_VEHICLE_MODE: String      = VEHICLE_MODE_REAL

        const val UPGRADE_STATUS_NEVER          = DEF_UPGRADE_STATUS
        const val UPGRADE_STATUS_ALREADY        = "already"
        const val UPGRADE_STATUS_DOWNLOADED     = "downloaded"
    }

    fun init(context: Context) {
        sharedApp.init(context)
    }
}