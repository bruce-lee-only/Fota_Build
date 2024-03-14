package com.carota.lib.executor.ue.node

import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.executor.ui.dialog.notify.DialogNotify
import org.koin.core.component.inject

class NodeNotify: NodeBase() {
    val dialog: DialogNotify by inject()
    val care = dialog.doOnBind(::handleUiEvent)

    override fun run() {
        super.run()
        dialog.doOnShow()
    }

    override fun handleUiEvent(event: String) {
        super.handleUiEvent(event)
        if (checkCare(care)){
            when(event){
                care!!.eventConfirmButtonClicked        -> {
                    Logger.drop("发送消息")
                    EventBus.globalEvent.post(INode.BUS_EVENT_NOTIFY_CHECK_CLICKED)
                    //fixme: need add bury point
                }
            }
        }
    }
}