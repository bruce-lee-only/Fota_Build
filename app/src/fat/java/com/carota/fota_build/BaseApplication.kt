package com.carota.fota_build

import GlobalBusEvent
import android.app.Application
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.carota.fota_build.broadcast.AssistReceiver
import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.status.db.AppDatabase
import com.carota.lib.status.shared.SharedManager
import com.carota.lib.ue.NodeCheck
import com.carota.lib.ue.UEHandler
import com.jeremyliao.liveeventbus.LiveEventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

abstract class BaseApplication:
    Application(),
    Observer<String>,
    IApplication,
    KoinComponent {

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
        LiveEventBus.config().autoClear(true).lifecycleObserverAlwaysActive(true).enableLogger(true)
        //todo: watch global event
        EventBus.globalEvent.observeForever(this)

        AssistReceiver.register(this)

        //todo: 初始化carota
        initSdk()
    }

    override fun onChanged(value: String) {
        Logger.info("Base application received global event: $value")
        if (onGlobalEventChange(value)) return
        when(value){
            GlobalBusEvent.EVENT_DISPLAY_DEBUG_ACTIVITY     -> {}
            GlobalBusEvent.EVENT_DISPLAY_ENGINE_ACTIVITY    -> {}
            GlobalBusEvent.EVENT_INIT_NODE_DONE             -> {
                val nodeCheck: NodeCheck by inject()
                nodeCheck.run()
            }
        }
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

    abstract fun onGlobalEventChange(event: String): Boolean
}