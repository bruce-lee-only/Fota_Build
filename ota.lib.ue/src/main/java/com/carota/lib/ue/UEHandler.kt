package com.carota.lib.ue

import android.content.Context
import com.carota.lib.common.uitls.Logger

internal val ueHandler by lazy { UEHandler() }
class UEHandler {
    internal var context: Context? = null

    companion object{
        fun injectApplicationContext(context: Context){
            ueHandler.context = context
        }
    }

    fun context(): Context?{
        return this.context ?: let{
            Logger.error("UE module have a null context, nothing be allowed")
            null
        }
    }
}