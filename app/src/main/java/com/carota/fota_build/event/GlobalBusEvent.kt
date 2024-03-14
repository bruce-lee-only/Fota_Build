package com.carota.fota_build.event

import android.content.Context
import androidx.lifecycle.Observer
import com.carota.fota_build.E0xApplication
import com.carota.fota_build.MainApplication
import com.carota.lib.common.uitls.Logger
import com.carota.lib.executor.ue.node.NodeCheck
import com.carota.lib.executor.ue.node.NodeDownload
import com.carota.lib.executor.ue.node.NodeInit
import com.carota.lib.executor.ue.node.NodeMainActivity
import com.carota.lib.executor.ue.node.NodeSelfUpgrade
import com.carota.lib.status.ocean.OceanSdkData
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.core.parameter.parametersOf

abstract class GlobalBusEvent(private val context: Context): Observer<String>, KoinComponent {

    override fun onChanged(value: String) {
        Logger.info("GlobalBusEvent received global event: $value")
        if (onGlobalEventChange(value)) return
        when(value){
            Event.BUS_EVENT_START_INIT_NODE             -> {
                val nodeInit: NodeInit by inject()
                nodeInit.run()
            }
            Event.BUS_EVENT_INIT_NODE_DONE              -> {
                val node: NodeSelfUpgrade by inject()
                node.run()
            }
            Event.BUS_EVENT_SELF_UPGRADE_NODE_DONE -> {
                if (OceanSdkData.INSTANCE.sdkSelfUpgradeResult.value == OceanSdkData.SDK_SELF_UPGRADE_NO_NEED){
                    val nodeCheck: NodeCheck by inject()
                    nodeCheck.run()
                }
            }
            Event.BUS_EVENT_CHECK_NODE_DONE        -> {
                if (OceanSdkData.INSTANCE.sdkCheckResult.value){
                    val nodeDownload: NodeDownload by inject()
                    nodeDownload.run()
                }
            }
            Event.BUS_EVENT_NOTIFY_CHECK_CLICKED    -> {
                val node: NodeMainActivity by inject()
                node.run()
            }
            Event.BUS_EVENT_DOWNLOAD_NODE_DONE  -> {

            }
        }
    }

    abstract fun onGlobalEventChange(event: String): Boolean
}