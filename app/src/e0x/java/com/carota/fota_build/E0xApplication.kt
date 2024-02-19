package com.carota.fota_build

import com.carota.lib.status.di.dbModule
import com.carota.lib.ue.NodeInit
import com.carota.lib.ue.di.ueModule
import com.carota.lib.ui.di.uiModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class E0xApplication: BaseApplication() {
    override fun minorVersion(): String {
        return ""
    }

    override fun onGlobalEventChange(event: String): Boolean {
        return false
    }

    override fun startInject() {
        startKoin {
            androidLogger(level = Level.NONE)
            androidContext(this@E0xApplication)
            modules(listOf(uiModule, dbModule, ueModule))
        }
    }

    /**
     * init sdk when apk start
     */
    override fun initSdk() {
        val nodeInit: NodeInit by inject()
        nodeInit.run()
    }

    override fun injectContext2Module() {
        super.injectContext2Module(this.applicationContext)
    }
}