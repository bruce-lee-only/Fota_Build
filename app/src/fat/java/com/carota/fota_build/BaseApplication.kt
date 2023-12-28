package com.carota.fota_build

import android.app.Application
import com.carota.lib.common.uitls.Logger

abstract class BaseApplication: Application(), IApplication{

    override fun onCreate() {
        super.onCreate()

        //todo:初始化-log系统
        Logger.initLogger(this)

        //todo: 打印当前配置版本信息+小版本信息
        Logger.info("APP VERSION: ${BuildConfig.EXIT_VERSION_CODE}${minorVersion()}")
    }

    abstract override fun minorVersion(): String
}