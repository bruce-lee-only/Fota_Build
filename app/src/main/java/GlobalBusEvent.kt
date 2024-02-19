import com.carota.fota_build.broadcast.AssistReceiver
import com.carota.lib.ue.INode

class GlobalBusEvent {
    companion object BusEvent{
        const val EVENT_DISPLAY_ENGINE_ACTIVITY = AssistReceiver.BUS_EVENT_BROADCAST_DISPLAY_ENGINE

        const val EVENT_DISPLAY_DEBUG_ACTIVITY  = AssistReceiver.BUS_EVENT_BROADCAST_DISPLAY_DEBUG

        const val EVENT_INIT_NODE_DONE          = INode.BUS_EVENT_INIT_NODE_DONE
    }
}