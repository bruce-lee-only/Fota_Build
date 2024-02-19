package com.carota.lib.common.uitls

import com.jeremyliao.liveeventbus.LiveEventBus
import com.jeremyliao.liveeventbus.core.Observable

class EventBus {
    companion object{
        val globalEvent by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ globalEvent() }

        private fun globalEvent(): Observable<String> {
            return LiveEventBus.get("global_event")
        }
    }
}