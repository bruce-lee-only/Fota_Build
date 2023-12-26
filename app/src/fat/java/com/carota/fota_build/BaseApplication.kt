package com.carota.fota_build

import android.app.Application
import com.carota.util.LogUtil

open class BaseApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        //todo:初始化-log系统
        LogUtil.initLogger(applicationContext)
    }
}