package com.carota.lib.common.uitls

import com.jeremyliao.liveeventbus.LiveEventBus
import com.jeremyliao.liveeventbus.core.Observable

class EventBus {
    companion object{
        val globalEvent by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ globalEvent() }

        val methodEvent by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { methodEvent() }

        private fun globalEvent(): Observable<String> {
            return LiveEventBus.get("global_event")
        }

        private fun methodEvent(): Observable<String> {
            return LiveEventBus.get("method_event")
        }
    }
}