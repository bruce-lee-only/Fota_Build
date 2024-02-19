package com.carota.lib.ue

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import org.koin.core.component.KoinComponent

abstract class NodeBase: KoinComponent, INode {
    val className: String = this::class.simpleName ?: ""

    val liveData: LiveDataUtil<Boolean> = LiveDataUtil()

    abstract fun run()

    //fixme: report burial point

    override fun checkUiAction(uiAction: String): String{
        return uiAction.takeIf { it.isEmpty() } ?: uiAction.apply { Logger.error("ui action is empty, can not handle this action") }
    }

    override fun handleUiEvent(event: String){
        Logger.warn("Ui event is not necessary for $className")
    }
}