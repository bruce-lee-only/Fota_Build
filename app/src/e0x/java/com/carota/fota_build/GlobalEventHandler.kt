package com.carota.fota_build

import android.content.Context
import com.carota.fota_build.event.GlobalBusEvent

class GlobalEventHandler(private val context: Context): GlobalBusEvent(context) {
    override fun onGlobalEventChange(event: String): Boolean {
        return false
    }

}