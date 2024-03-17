package com.carota.lib.executor.ue.node

import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.sdk.CarotaSdkHelper
import com.carota.lib.status.ocean.OceanSdkData
import com.carota.lib.executor.ue.pump.PumpDataSelfUpgrade

class NodeSelfUpgrade: NodeBase() {
    private val pumpDataSelfUpgrade = PumpDataSelfUpgrade()
    override fun run() { super.run()
        pumpDataSelfUpgrade.let {
            if (it.isUpgradeRunning) {
                Logger.warn("run self upgrade fail, upgrade is running")
                return
            }
            if (it.isScheduled) {
                Logger.warn("run self upgrade fail, has schedule task")
                return
            }
            if (it.isRebootDisplayed) {
                Logger.warn("run self upgrade fail, reboot view displayed")
                return
            }

            if (it.vehicleVin.isNotEmpty()) {
                Logger.info("self upgrade get vehicle vin: ${it.vehicleVin}")
                CarotaSdkHelper.carotaSelfUpgrade(it.vehicleVin){ result ->
                    when(result){
                        OceanSdkData.SDK_SELF_UPGRADE_SUCCESS    -> { Logger.info("self upgrade success") }
                        OceanSdkData.SDK_SELF_UPGRADE_FAILED     -> { Logger.info("self upgrade failed") }
                        OceanSdkData.SDK_SELF_UPGRADE_NO_NEED    -> { Logger.info("self upgrade no need") }
                    }
                }
            }else{
                Logger.warn("run self upgrade fail, get vin error")
            }

            printNodeFinish(pumpDataSelfUpgrade.toString())
            EventBus.globalEvent.post(INode.BUS_EVENT_SELF_UPGRADE_NODE_DONE)
        }
    }
}