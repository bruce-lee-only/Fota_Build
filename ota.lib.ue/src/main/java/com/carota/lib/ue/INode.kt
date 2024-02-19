package com.carota.lib.ue

interface INode {

    companion object{
        const val BUS_EVENT_INIT_NODE_DONE = "init_node_done"

        const val BUS_EVENT_CHECK_NODE_DONE = "check_node_done"
    }

    fun checkUiAction(uiAction: String): String

    fun handleUiEvent(event: String)
}