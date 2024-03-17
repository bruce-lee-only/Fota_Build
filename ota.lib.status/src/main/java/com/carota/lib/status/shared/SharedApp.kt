package com.carota.lib.status.shared

import android.content.Context
import android.content.SharedPreferences
import com.carota.lib.status.shared.SharedManager.Statical.DEF_SCHEDULE_TIME
import com.carota.lib.status.shared.SharedManager.Statical.DEF_UPGRADE_STATUS
import com.carota.lib.status.shared.SharedManager.Statical.DEF_VEHICLE_MODE
import com.carota.lib.status.shared.SharedManager.Statical.TAG_UPGRADE_STATUS

class SharedApp {

    private lateinit var shared     : SharedPreferences

    /**
     * we must invoke this function, before use shared
     * @param context Context
     */
    fun init(context: Context) {
        shared = context.getSharedPreferences("sp.carota.update", Context.MODE_PRIVATE)
    }

    /**
     * reboot display type
     * use this value to choose witch dialog should be showed
     */
    var rebootDisplayContent: String? = null
        get()       = shared.getString(SharedManager.TAG_REBOOT_DISPLAY, SharedManager.DEF_REBOOT_DISPLAY)
        set(value)  = with(shared.edit()){
            this.putString(SharedManager.TAG_REBOOT_DISPLAY, value)
            this.apply()
            field = value
        }

    /**
     * apk run mode
     */
    var sysRunMode: String = DEF_VEHICLE_MODE
        get()       = shared.getString(SharedManager.TAG_VEHICLE_MODE, DEF_VEHICLE_MODE) ?: DEF_VEHICLE_MODE
        set(value)  = with(shared.edit()){
            this.putString(SharedManager.TAG_VEHICLE_MODE, value)
            this.apply()
            field = value
        }

    var scheduleTime: Long  = DEF_SCHEDULE_TIME
        get()       = shared.getLong(SharedManager.TAG_SCHEDULE_TIME, DEF_SCHEDULE_TIME)
        set(value)  = with(shared.edit()){
            this.putLong(SharedManager.TAG_VEHICLE_MODE, value)
            this.apply()
            field = value
        }

    var upgradeStatus : String = DEF_UPGRADE_STATUS
        get()       = shared.getString(TAG_UPGRADE_STATUS, DEF_UPGRADE_STATUS) ?: DEF_UPGRADE_STATUS
        set(value)  = with(shared.edit()){
            putString(TAG_UPGRADE_STATUS, value)
            apply()
            field = value
        }

    /**
     * is in ota mode
     * maybe save this value in db
     */

    /**
     * update  mode
     * maybe save this value in db
     */
}