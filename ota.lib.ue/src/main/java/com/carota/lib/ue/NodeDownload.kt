package com.carota.lib.ue

import com.carota.core.ISession
import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.sdk.CarotaSdkHelper
import com.carota.lib.ue.pump.PumpDataDownload

class NodeDownload: NodeBase() {
    private val pumpDataDownload = PumpDataDownload()
    override fun run() { super.run()
        pumpDataDownload.downloadCareResult.bindObserver(className, liveData)?.setIsContinue(isContinue = false)

        liveData.observeForever{
            Logger.info("sdk download finished, result: ${pumpDataDownload.downloadCareResult.oceanValue()}")
            if (pumpDataDownload.downloadCareResult.oceanValue()){
                when(pumpDataDownload.updateType){
                    ISession.MODE_USER_CONFIRM              -> {
                        EventBus.methodEvent.post(INode.BUS_EVENT_SHOW_RED_POINT)
                    }
                    ISession.MODE_USER_LIMIT                -> {
                        EventBus.methodEvent.post(INode.BUS_EVENT_SHOW_RED_POINT)
                    }
                    ISession.MODE_AUTO_INSTALL_SCHEDULE     -> {}
                }
            }
            printNodeFinish(pumpDataDownload.toString())
            EventBus.globalEvent.post(INode.BUS_EVENT_DOWNLOAD_NODE_DONE)
        }

        //fixme: factory maybe need display download dialog
        if (pumpDataDownload.isFactory) { Logger.info("factory mode display downloading dialog") }

        CarotaSdkHelper.carotaSdkDownloadUpgradeNow()
    }
}