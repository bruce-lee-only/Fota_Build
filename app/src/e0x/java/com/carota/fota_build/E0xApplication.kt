package com.carota.fota_build

import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class E0xApplication: BaseApplication() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun minorVersion(): String {
        return ".01"
    }
}