package com.carota.lib.status.ocean

data class SystemData(private val id: Int = 2){
    /**
     * 当前是否是debug模式
     */
    var systemIsDebugMode: Boolean = false

    /**
     * 当前是否在工程模式
     */
    var systemIsEngineMode: Boolean = false
}
