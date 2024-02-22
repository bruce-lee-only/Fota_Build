package com.carota.lib.ue

interface INode {

    companion object{
        const val BUS_EVENT_INIT_NODE_DONE          = "init_node_done"

        const val BUS_EVENT_CHECK_NODE_DONE         = "check_node_done"

        const val BUS_EVENT_SELF_UPGRADE_NODE_DONE  = "self_upgrade_node_done"

        const val BUS_EVENT_DOWNLOAD_NODE_DONE      = "download_node_done"

        const val BUS_EVENT_SHOW_RED_POINT          = "show_red_point"

        const val BUS_EVENT_HIDE_RED_POINT          = "hide_red_point"


    }

    fun checkUiAction(uiAction: String): String

    fun handleUiEvent(event: String)

    fun run()
}