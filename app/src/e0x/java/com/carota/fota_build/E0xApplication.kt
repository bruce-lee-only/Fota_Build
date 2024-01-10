package com.carota.fota_build

import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class E0xApplication: BaseApplication() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun minorVersion(): String {
        return ""
    }

    override fun injectContext2Module() {
        super.injectContext2Module(this.applicationContext)
    }
}