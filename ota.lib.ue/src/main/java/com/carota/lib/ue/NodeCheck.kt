package com.carota.lib.ue

import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.sdk.CarotaSdkHelper
import com.carota.lib.ue.pump.PumpDataCheck

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
            var ready = true
            if (checkPumper.checkResult){
                if (checkPumper.isSilentUpgrade) {
                    if (checkPumper.isScheduleEventPass){
                        ready = false
                        Logger.error("silent schedule event check fail, send bury event")
                        //fixme: need add bury event
                    }
                }
            }else ready = false
            printNodeFinish(checkPumper.toString())
            if (ready) EventBus.globalEvent.post(INode.BUS_EVENT_CHECK_NODE_DONE)
        }

        CarotaSdkHelper.carotaSdkNormalCheck()
    }

    private fun silentUpgradeCheck(): Boolean{
        return true
    }
}