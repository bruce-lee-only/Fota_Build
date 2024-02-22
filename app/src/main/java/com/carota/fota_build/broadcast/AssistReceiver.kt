package com.carota.fota_build.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger

class AssistReceiver : BroadcastReceiver() {

    companion object {
        const val BUS_EVENT_BROADCAST_DISPLAY_ENGINE: String    = "display_engine_activity"
        const val BUS_EVENT_BROADCAST_DISPLAY_DEBUG: String     = "display_debug_activity"

        const val ENGINE_MODE_BROADCAST_ACTION        = "com.carota.chery.engine.action"
        const val DEBUG_MODE_BROADCAST_ACTION         = "com.carota.chery.debug.action"

        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun register(context: Context) {

            val debugIntent     = IntentFilter()
            val engineIntent    = IntentFilter()

            //todo: display debug activity -> am broadcast -a com.carota.chery.debug.action
            debugIntent.addAction(DEBUG_MODE_BROADCAST_ACTION)
            //todo: display engine activity -> am broadcast -a com.carota.chery.engine.action
            engineIntent.addAction(ENGINE_MODE_BROADCAST_ACTION)

            context.registerReceiver(AssistReceiver(), debugIntent, Context.RECEIVER_NOT_EXPORTED)
            context.registerReceiver(AssistReceiver(), engineIntent, Context.RECEIVER_NOT_EXPORTED)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {

        intent ?: let {
            Logger.error("Broadcast Receiver receive null intent!")
            return@onReceive
        }

        context ?: let {
            Logger.error("Broadcast Receiver receive null context!")
            return@onReceive
        }

        Logger.info("Broadcast Receiver Broadcast Action:" + intent.action)

        when(intent.action){
            DEBUG_MODE_BROADCAST_ACTION     -> {
                EventBus.methodEvent.post(BUS_EVENT_BROADCAST_DISPLAY_ENGINE)
            }
            ENGINE_MODE_BROADCAST_ACTION    -> {
                EventBus.methodEvent.post(BUS_EVENT_BROADCAST_DISPLAY_DEBUG)
            }
            else    -> {
                Logger.error("Broadcast Receiver receive unknown Action")
            }
        }
    }
}