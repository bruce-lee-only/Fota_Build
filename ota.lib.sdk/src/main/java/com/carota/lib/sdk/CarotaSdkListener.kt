package com.carota.lib.sdk

import com.carota.hmi.callback.ICall
import com.carota.hmi.callback.ICallBack
import com.carota.hmi.callback.IExitOtaCall
import com.carota.lib.status.ocean.OceanSdkData
import com.carota.lib.status.river.sdk.RiverInit
import com.momock.util.Logger

class CarotaSdkListener: ICallBack {
    override fun onInitStart() {
        Logger.info("carota sdk init started")
        RiverInit.INSTANCE.initStep.value = OceanSdkData.SDK_INIT_STEP_START
    }

    override fun onInitEnd() {
        Logger.info("carota sdk init end")
        RiverInit.INSTANCE.let {
            it.initStep.value       = OceanSdkData.SDK_INIT_STEP_FINISH
            it.isUpdateRun.value    = CarotaSdkHelper.carotaSdkIsUpgradeTriggered()
            //todo: update is running, resume must be true
            it.isResume.value       = it.isUpdateRun.value
            //todo: this line code must run at last, please add code before this
            it.isInitFinish.value   = true
        }
    }

    override fun factory(): ICallBack.IFactory {
        TODO("Not yet implemented")
    }

    override fun schedule(): ICallBack.ISchedule {
        TODO("Not yet implemented")
    }

    override fun upgradeNow(): ICallBack.IUpgradeNow {
        TODO("Not yet implemented")
    }

    override fun check(): ICall {
        return CarotaSdkListenerHandler().mICheck
    }

    override fun download(): ICall {
        TODO("Not yet implemented")
    }

    override fun enterOta(): ICall {
        TODO("Not yet implemented")
    }

    override fun condition(): ICall {
        TODO("Not yet implemented")
    }

    override fun install(): ICall {
        TODO("Not yet implemented")
    }

    override fun exitOta(): IExitOtaCall {
        TODO("Not yet implemented")
    }

    override fun taskTimeOut(): ICall {
        TODO("Not yet implemented")
    }

    override fun setTime(): ICall {
        TODO("Not yet implemented")
    }
}