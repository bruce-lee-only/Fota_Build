package com.carota.fota_build

import android.os.Build
import androidx.annotation.RequiresApi
import com.carota.fota_build.event.Event
import com.carota.lib.common.uitls.EventBus
import com.carota.lib.slave.di.slaveModule
import com.carota.lib.status.di.dbModule
import com.carota.lib.ue.di.ueModule
import com.carota.lib.ui.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class E0xApplication: BaseApplication() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
    }

    override fun minorVersion(): String {
        return ""
    }

    override fun startInject() {
        startKoin {
            androidLogger(level = Level.NONE)
            androidContext(this@E0xApplication)
            modules(listOf(uiModule, dbModule, ueModule, slaveModule))
        }
    }

    override fun initSdk() {
        EventBus.globalEvent.post(Event.BUS_EVENT_START_INIT_NODE)
    }

    override fun injectContext2Module() {
        super.injectContext2Module(this.applicationContext)
    }
}