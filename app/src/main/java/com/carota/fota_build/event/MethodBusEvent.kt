package com.carota.fota_build.event

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Observer
import com.carota.lib.common.uitls.Logger
import com.carota.lib.executor.ui.activity.debugActivity.DebugActivity
import com.carota.lib.executor.ui.activity.engineActivity.EngineActivity
import com.carota.lib.slave.SlaveHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class MethodBusEvent(private val context: Context): Observer<String>, KoinComponent {

    override fun onChanged(value: String) {
        Logger.info("MethodBusEvent received global event: $value")
        if (onGlobalEventChange(value)) return
        when(value){
            Event.BUS_EVENT_DISPLAY_DEBUG_ACTIVITY      -> {
                val intent = Intent(context, DebugActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            Event.BUS_EVENT_DISPLAY_ENGINE_ACTIVITY     -> {
                val intent = Intent(context, EngineActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            Event.BUS_EVENT_SHOW_RED_POINT      -> {
                val display: SlaveHelper by inject()
                display.displayRedPoint()
            }
            Event.BUS_EVENT_HIDE_RED_POINT      -> {
                val hide: SlaveHelper by inject()
                hide.hideRedPoint()
            }
        }
    }

    abstract fun onGlobalEventChange(event: String): Boolean
}