package com.carota.lib.executor.ue.node

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import com.carota.lib.executor.ui.BaseCare
import org.koin.core.component.KoinComponent

abstract class NodeBase: KoinComponent, INode {
    val className: String = this::class.simpleName ?: ""

    val liveData: LiveDataUtil<Boolean> = LiveDataUtil()

    //fixme: report burial point

    override fun run() {
        Logger.info("OTA run node: $className")
    }

    override fun checkUiAction(uiAction: String): String{
        return uiAction.takeIf { it.isEmpty() } ?: uiAction.apply { Logger.error("ui action is empty, can not handle this action") }
    }

    override fun handleUiEvent(event: String){
        Logger.info("hand Ui event for $className by event: $event")
    }

    private fun printString(data: String){
        Logger.info("$className string data: $data")
    }

    fun printNodeFinish(data: String = ""){
        printString(data)
        Logger.info("OTA run node: $className finish")
    }

    fun checkCare(care: BaseCare?): Boolean{
        return if (care != null){ true } else {
            Logger.warn("$className check node care error, care is null!")
            false
        }
    }

}