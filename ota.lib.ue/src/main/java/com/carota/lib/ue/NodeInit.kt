package com.carota.lib.ue

import android.content.Context
import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import com.carota.lib.sdk.CarotaSdkHelper
import com.carota.lib.status.pump.PumpDataInit

class NodeInit(private val context: Context): NodeBase(){
    private val initPumper = PumpDataInit()

    override fun run() {
        initPumper.initCareIsFinish.bindObserver(className, liveData)?.setIsContinue(isContinue = false)
        liveData.observeForever{
            Logger.info("sdk init finish, result: ${initPumper.initCareIsFinish.value}")
            EventBus.globalEvent.post(INode.BUS_EVENT_INIT_NODE_DONE)
        }
        CarotaSdkHelper.carotaSdkInit(context)
    }
}