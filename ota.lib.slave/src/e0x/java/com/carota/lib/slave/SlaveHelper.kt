package com.carota.lib.slave

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import com.carota.lib.common.uitls.FormatUtil
import com.carota.lib.common.uitls.Logger
import com.carota.lib.slave.schedule.ScheduleEvent

class SlaveHelper(private val context: Context): BaseSlaveHelper() {

    private val mRedPointKey    = "def_ota_upgrade_state"
    private val mRedPointShow   = 1
    private val mRedPointHide   = 0

    override fun hideRedPoint() {
        try {
            val ret = Settings.Global.putInt(context.contentResolver, mRedPointKey, mRedPointHide)
            Logger.info("RedPoint Global hideRedPoint result: $ret")
        }catch (e: Exception){
            Logger.error("RedPoint Global hideRedPoint Exception: $e")
        }
    }

    override fun displayRedPoint() {
        try {
            val ret = Settings.Global.putInt(context.contentResolver, mRedPointKey, mRedPointShow)
            Logger.info("RedPoint Global showRedPoint result: $ret")
        }catch (e: Exception){
            Logger.error("RedPoint Global showRedPoint Exception: $e")
        }
    }

    override fun checkScheduleEvent(startTime: Long, endTime: Long):Bundle? {
        return ScheduleEvent().loadScheduleEvent()?.let {
            var bundle: Bundle? = null
            if (it.isNotEmpty()){
                bundle = Bundle()
                it.forEach{ info ->
                    if (info.start.coerceAtLeast(startTime) <= info.end.coerceAtMost(endTime)) {
                        bundle.putString("name", info.name)
                        bundle.putString("start", FormatUtil.formatHour(info.start))
                        bundle.putString("end", FormatUtil.formatHour(info.end))
                    }
                }
            }
            bundle
        }
    }

}