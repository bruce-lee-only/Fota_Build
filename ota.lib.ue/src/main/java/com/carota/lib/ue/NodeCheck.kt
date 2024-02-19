package com.carota.lib.ue

import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.sdk.CarotaSdkHelper
import com.carota.lib.status.pump.PumpDataCheck

class NodeCheck: NodeBase() {
    private val checkPumper = PumpDataCheck()

    override fun run() {
        checkPumper.checkCareIsFinish.bindObserver(className, liveData)?.setIsContinue(isContinue = false)
        liveData.observeForever{
            Logger.info("sdk check finish, result: ${checkPumper.checkCareIsFinish.value}")
            EventBus.globalEvent.post(INode.BUS_EVENT_CHECK_NODE_DONE)
        }
        CarotaSdkHelper.carotaSdkNormalCheck()
    }
}