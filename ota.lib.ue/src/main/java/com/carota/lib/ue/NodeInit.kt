package com.carota.lib.ue

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import com.carota.lib.status.node.NodeDataInit

class NodeInit: NodeBase() {
    private val nodeDataInit = NodeDataInit()

    override fun run() {
        val liveData: LiveDataUtil<Boolean> = LiveDataUtil()
        nodeDataInit.initCareIsFinish.bindObserver(className, liveData)?.setIsContinue(className,false)
        liveData.observeForever(){
            Logger.needDel("liveData ret: $it")
            nodeDataInit.initCareIsFinish.unBindObserver(className)
        }
    }
}