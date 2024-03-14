package com.carota.lib.sdk

import com.carota.hmi.callback.IHmiCallback
import com.carota.hmi.callback.IHmiPolicyManager
import com.carota.hmi.type.HmiTaskType
import com.carota.hmi.type.UpgradeType
import com.momock.util.Logger

val carotaSdkListener: IHmiCallback get() = object : IHmiCallback{
    var policyManager: IHmiPolicyManager? = null

    override fun updatePolicyManager(manager: IHmiPolicyManager?) {
        Logger.info("carota sdk update policy manager")
        policyManager = manager
    }

    override fun startRunPolicy(type: UpgradeType?) {
        Logger.info("carota sdk start run policy: $type")
        policyManager?.startPolicy()
    }

    override fun startRunPolicyError(type: UpgradeType?) {
        Logger.error("carota sdk start run policy error: $type")
    }

    override fun taskStart(taskType: HmiTaskType?) {
        Logger.info("carota sdk start task type: $taskType")
        taskType?.let {
            when(it){
                HmiTaskType.check   -> {

                }
                else    -> {
                    Logger.warn("carota sdk start without handle the task type")
                }
            }
        } ?: Logger.error("carota sdk task start, but task type is null")
    }

    override fun taskEnd(taskType: HmiTaskType?, result: IHmiCallback.IHmiResult?) {
        if (taskType == HmiTaskType.check){
            policyManager?.endPolicy()
        }
    }

    override fun endRunPolicy() {
        TODO("Not yet implemented")
    }

    override fun findTimeChange(time: Long) {
        TODO("Not yet implemented")
    }
    //    override fun onInitStart() {
//        Logger.info("carota sdk init started")
//        RiverInit.INSTANCE.initStep.value = OceanSdkData.SDK_STEP_START
//    }
//
//    override fun onInitEnd() {
//        Logger.info("carota sdk init end")
//        RiverInit.INSTANCE.let {
//            it.initStep.value       = OceanSdkData.SDK_STEP_FINISH
//            it.isUpdateRun.value    = CarotaSdkHelper.carotaSdkIsUpgradeTriggered()
//            //todo: update is running, resume must be true
//            it.isResume.value       = it.isUpdateRun.value
//            //todo: this line code must run at last, please add code before this
//            it.isInitFinish.value   = true
//        }
//    }
//
//    override fun factory(): ICallBack.IFactory {
//        TODO("Not yet implemented")
//    }
//
//    override fun schedule(): ICallBack.ISchedule {
//        TODO("Not yet implemented")
//    }
//
//    override fun upgradeNow(): ICallBack.IUpgradeNow {
//        TODO("Not yet implemented")
//    }
//
//    override fun check(): ICall {
//        return CarotaSdkListenerHandler().mICheck
//    }
//
//    override fun download(): ICall {
//        return CarotaSdkListenerHandler().mIDownload
//    }
//
//    override fun enterOta(): ICall {
//        TODO("Not yet implemented")
//    }
//
//    override fun condition(): ICall {
//        TODO("Not yet implemented")
//    }
//
//    override fun install(): ICall {
//        TODO("Not yet implemented")
//    }
//
//    override fun exitOta(): IExitOtaCall {
//        TODO("Not yet implemented")
//    }
//
//    override fun taskTimeOut(): ICall {
//        TODO("Not yet implemented")
//    }
//
//    override fun setTime(): ICall {
//        TODO("Not yet implemented")
//    }

}