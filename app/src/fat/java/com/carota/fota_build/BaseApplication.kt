package com.carota.fota_build

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.carota.fota_build.broadcast.AssistReceiver
import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.executor.ExecutorHandler
import com.carota.lib.status.db.AppDatabase
import com.carota.lib.status.shared.SharedManager
import com.jeremyliao.liveeventbus.LiveEventBus

abstract class BaseApplication:
    Application(),
    IApplication{

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()

        //todo:初始化-log系统
        Logger.initLogger(this)

        //todo: init dependency injection
        startInject()

        //todo: 打印当前配置版本信息+小版本信息
        Logger.info("APP VERSION: ${BuildConfig.EXIT_VERSION_CODE}${minorVersion()}")

        //todo: init shared store
        SharedManager().init(this)

        //todo: inject context to all module
        injectContext2Module()

        //todo: init Database
        AppDatabase.setDatabase(this)

        //todo: init liveEventBus
        analysisEventBus()

        //todo: init broadcast
        AssistReceiver.register(this)

        //todo: 初始化carota
        initSdk()
    }

    override fun injectContext2Module(context: Context){
        //todo: inject child context to module
        ExecutorHandler.injectApplicationContext(context)
    }

    private fun analysisEventBus(){
        LiveEventBus.config().autoClear(true).lifecycleObserverAlwaysActive(true).enableLogger(true)
        EventBus.globalEvent.observeForever(GlobalEventHandler(this))
        EventBus.methodEvent.observeForever(MethodEventHandler(this))
    }

    /**
     * 附加打印的版本信息
     */
    abstract override fun minorVersion(): String

    abstract fun injectContext2Module()
}