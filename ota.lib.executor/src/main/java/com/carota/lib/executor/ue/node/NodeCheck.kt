package com.carota.lib.executor.ue.node

import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.sdk.CarotaSdkHelper
import com.carota.lib.executor.ue.pump.PumpDataCheck

class NodeCheck: NodeBase() {
    private val checkPumper = PumpDataCheck()
    override fun run() { super.run()
        checkPumper.checkCareResult.bindObserver(className, liveData)?.setIsContinue(isContinue = false)

        if (checkPumper.isScheduled || checkPumper.isRebootDisplayed){
            Logger.error("return node check")
            printNodeFinish(checkPumper.toString())
            return
        }

        liveData.observeForever{
            Logger.info("sdk check finished, result: ${checkPumper.checkResult}")
            val ready: Boolean = if (checkPumper.checkResult){
                if (checkPumper.isSilentUpgrade) {
                    val pass = checkPumper.isScheduleEventPass
                    //fixme: need add bury event
                    if (!pass) Logger.error("silent schedule event check fail, send bury event")
                    pass
                }else true
            }else false
            printNodeFinish(checkPumper.toString())
            if (ready) EventBus.globalEvent.post(INode.BUS_EVENT_CHECK_NODE_DONE)
        }

        CarotaSdkHelper.carotaSdkNormalCheck()
    }

    private fun silentUpgradeCheck(): Boolean{
        return true
    }
}