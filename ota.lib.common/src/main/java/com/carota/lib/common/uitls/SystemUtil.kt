package com.carota.lib.common.uitls

import android.os.SystemClock

class SystemUtil {
    companion object{

        /**
         * 获取系统当前时间
         * 单位：ms
         * @return Long
         */
        fun systemTime(): Long{
            return System.currentTimeMillis()
        }

        /**
         * 获取系统开机到当前的时间
         * 单位：纳秒
         * @return Long
         */
        fun systemElapseTime(): Long{
            return SystemClock.elapsedRealtime()
        }
    }
}