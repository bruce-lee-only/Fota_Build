package com.carota.fota_build.event

import com.carota.fota_build.broadcast.AssistReceiver
import com.carota.lib.executor.ue.node.INode

class Event {
    companion object BusEvent{

        const val BUS_EVENT_DISPLAY_ENGINE_ACTIVITY = AssistReceiver.BUS_EVENT_BROADCAST_DISPLAY_ENGINE

        const val BUS_EVENT_DISPLAY_DEBUG_ACTIVITY  = AssistReceiver.BUS_EVENT_BROADCAST_DISPLAY_DEBUG

        const val BUS_EVENT_START_INIT_NODE         = "start_init_node"

        const val BUS_EVENT_INIT_NODE_DONE          = INode.BUS_EVENT_INIT_NODE_DONE

        const val BUS_EVENT_SELF_UPGRADE_NODE_DONE  = INode.BUS_EVENT_SELF_UPGRADE_NODE_DONE

        const val BUS_EVENT_CHECK_NODE_DONE         = INode.BUS_EVENT_CHECK_NODE_DONE

        const val BUS_EVENT_DOWNLOAD_NODE_DONE      = INode.BUS_EVENT_DOWNLOAD_NODE_DONE

        const val BUS_EVENT_SHOW_RED_POINT          = INode.BUS_EVENT_SHOW_RED_POINT

        const val BUS_EVENT_HIDE_RED_POINT          = INode.BUS_EVENT_HIDE_RED_POINT

        const val BUS_EVENT_NOTIFY_CHECK_CLICKED    = INode.BUS_EVENT_NOTIFY_CHECK_CLICKED
    }
}