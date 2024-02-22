package com.carota.lib.ue

import android.content.Context
import com.carota.lib.common.uitls.CountdownUtil
import com.carota.lib.common.uitls.EventBus
import com.carota.lib.common.uitls.Logger
import com.carota.lib.sdk.CarotaSdkHelper
import com.carota.lib.ue.pump.PumpDataInit

class NodeInit(private val context: Context): NodeBase(){
    private val initPumper = PumpDataInit()

    override fun run() { super.run()
        initPumper.initCareIsFinish.bindObserver(className, liveData)?.setIsContinue(isContinue = false)
        liveData.observeForever{
            Logger.info("sdk init finish, result: ${initPumper.initCareIsFinish.value}")
            //todo: keep 60 second for remote controller
            //fixme: change time to 60
            CountdownUtil.countdown(5){
                printNodeFinish(initPumper.toString())
                EventBus.globalEvent.post(INode.BUS_EVENT_INIT_NODE_DONE)
            }
        }
        CarotaSdkHelper.carotaSdkInit(context)
    }
}