package com.carota.lib.common.uitls

import android.content.Context
import com.carota.util.LogUtil
import com.momock.util.Logger

class Logger {
    companion object{
        fun initLogger(applicationContext: Context){
            LogUtil.initLogger(applicationContext)
        }

        fun info(msg: String?, vararg args: Any?) { Logger.info(msg, args) }

        fun debug(msg: String?, vararg args: Any?) { Logger.debug(msg, args) }

        fun warn(msg: String?, vararg args: Any?) { Logger.warn(msg, args) }

        fun error(msg: String?, vararg args: Any?) { Logger.error(msg, args) }

        fun error(e: Throwable) { Logger.error(e) }
    }
}