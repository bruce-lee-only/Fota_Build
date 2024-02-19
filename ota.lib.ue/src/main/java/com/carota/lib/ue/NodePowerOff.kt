package com.carota.lib.ue

import com.carota.lib.common.uitls.LaunchUtil
import com.carota.lib.common.uitls.Logger
import com.carota.lib.ui.toast.care.DialogAttentionCare
import com.carota.lib.ui.toast.attention.DialogAttention
import kotlinx.coroutines.delay
import org.koin.core.component.inject

class NodePowerOff: NodeBase() {
    override fun run() {
        val dialog: DialogAttention by inject()
        val care = dialog.doOnBind(::handleUiEvent)
        care?.let {
            it.displayOption = DialogAttentionCare.DisplayOption.ROLLBACK_EXIT_OTA_FAIL
            dialog.doOnShow()

            LaunchUtil.instance.launch {
                delay(3_000)
                it.displayOption = DialogAttentionCare.DisplayOption.UPGRADED_POWER_OFF_FAIL
                Logger.drop("start second display")
                it.sync()

                delay(3_000)
                it.displayOption = DialogAttentionCare.DisplayOption.UPGRADED_EXIT_OTA_FAIL
                Logger.drop("start third display")
                it.sync()
            }
        }
    }

    override fun handleUiEvent(event: String) {
        Logger.drop("$className handle ui event: $event")
    }
}