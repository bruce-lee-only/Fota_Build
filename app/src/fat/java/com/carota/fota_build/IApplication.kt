package com.carota.fota_build

import android.content.Context

interface IApplication {
    fun minorVersion(): String

    fun injectContext2Module(context: Context)
}