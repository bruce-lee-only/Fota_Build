package com.carota.fota_build

import android.app.Application
import android.content.Context
import com.carota.lib.common.uitls.Logger
import com.carota.lib.ue.NodeInit
import com.carota.lib.ue.UEHandler

abstract class BaseApplication: Application(), IApplication{

    override fun onCreate() {
        super.onCreate()

        //todo:初始化-log系统
        Logger.initLogger(this)

        //todo: 打印当前配置版本信息+小版本信息
        Logger.info("APP VERSION: ${BuildConfig.EXIT_VERSION_CODE}${minorVersion()}")

        //todo: inject context to all module
        injectContext2Module()

        //todo: 初始化carota
        NodeInit().run()
    }

    override fun injectContext2Module(context: Context){
        //todo: inject child context to module
        UEHandler.injectApplicationContext(context)
    }

    /**
     * 附加打印的版本信息
     */
    abstract override fun minorVersion(): String

    abstract fun injectContext2Module()
}