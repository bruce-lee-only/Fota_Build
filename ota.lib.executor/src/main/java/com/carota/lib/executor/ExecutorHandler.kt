package com.carota.lib.executor

import android.content.Context
import com.carota.lib.common.uitls.Logger

internal val executorHandler by lazy { ExecutorHandler() }
class ExecutorHandler {
    internal var context: Context? = null

    companion object{
        fun injectApplicationContext(context: Context){
            executorHandler.context = context
        }
    }

    fun context(): Context?{
        return this.context ?: let{
            Logger.error("UE module have a null context, nothing be allowed")
            null
        }
    }
}