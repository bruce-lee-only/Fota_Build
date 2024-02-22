package com.carota.fota_build

import android.content.Context
import com.carota.fota_build.event.MethodBusEvent
import org.checkerframework.checker.units.qual.C

class MethodEventHandler(private val context: Context): MethodBusEvent(context) {
    override fun onGlobalEventChange(event: String): Boolean {
        return false
    }
}