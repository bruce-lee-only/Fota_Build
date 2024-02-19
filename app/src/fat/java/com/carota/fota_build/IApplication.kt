package com.carota.fota_build

import android.content.Context

interface IApplication {

    /**
     * create minor version for you app, usual, use this for create version for carota QA
     * @return String
     */
    fun minorVersion(): String

    fun injectContext2Module(context: Context)

    /**
     * init all kion inject, if you want to use the it
     */
    fun startInject()

    /**
     * for choose init carota sdk chanel
     * chanel one: apk started
     * chanel two: machine stared broadcast
     */
    fun initSdk()
}