package com.carota.lib.sdk

import com.carota.core.data.UpdateSession
import com.carota.lib.common.uitls.SystemUtil
import com.carota.lib.status.ocean.OceanSdkData
import com.carota.lib.status.river.sdk.RiverCheck
import com.carota.lib.status.river.sdk.RiverDownload
import com.momock.util.Logger
import java.util.Calendar

class CarotaSdkListenerHandler {
//    val mICheck : ICall     get() = object : ICall{
//            override fun onStart(upgradeType: UpgradeType?) {
//                Logger.info("carota sdk check onStart upgradeType: $upgradeType")
//                RiverCheck.INSTANCE.checkStep.value = OceanSdkData.SDK_STEP_START
//            }
//
//            override fun onError(upgradeType: UpgradeType?, error: Int) {
//                Logger.info("carota sdk check onError: $error")
//                RiverCheck.INSTANCE.checkStep.value = OceanSdkData.SDK_STEP_ERROR
//            }
//
//            override fun onEnd(upgradeType: UpgradeType?, success: Boolean, status: IStatus?) {
//                Logger.info("carota sdk check onEnd: $success")
//                RiverCheck.INSTANCE.checkStep.value = OceanSdkData.SDK_STEP_FINISH
//                if (success && status != null && analysisHmiStatus(status as HmiStatus)){
//                    RiverCheck.INSTANCE.checkResult.value = true
//                }else{
//                    Logger.error("carota sdk check task failed")
//                    RiverCheck.INSTANCE.checkResult.value = false
//                }
//            }
//        }
//
//    val mIDownload : IDownloadCall      get() = object : IDownloadCall {
//        override fun onStart(upgradeType: UpgradeType?) {
//            Logger.info("carota sdk download onStart")
//        }
//
//        override fun onError(upgradeType: UpgradeType?, error: Int) {
//            Logger.error("carota sdk download onError: $error")
//        }
//
//        override fun onEnd(upgradeType: UpgradeType?, success: Boolean, status: IStatus?) {
//            Logger.info("carota sdk download onEnd: $success")
//            RiverDownload.INSTANCE.downloadResult.value = success
//        }
//
//        override fun onDownloading(upgradeType: UpgradeType?, type: EventType?, mStatus: IStatus?) {
//            Logger.info("carota sdk downloading: progress->${mStatus?.downloadPro}  speed->${mStatus?.downloadSpeed}")
//        }
//
//    }
//
//    private fun analysisHmiStatus(status: HmiStatus): Boolean{
//        val session = status.session as UpdateSession
//        session.let {
//            RiverCheck.INSTANCE.updateType.value = it.mode
//            RiverCheck.INSTANCE.appointmentTime.value = it.appointmentTime()
//            return true
//        }
//    }

    /**
     * get silent schedule time
     * @receiver UpdateSession
     * @return Long: keep until hour
     */
    private fun UpdateSession.appointmentTime(): Long{
        var st = this.rawData.optLong("appointment_time", 0) * 1000 //ms
        if (st == 0L) { st = (24 * 60 * 60 * 1000).toLong() }

        val calendar = Calendar.getInstance()
        val hour = calendar[Calendar.HOUR_OF_DAY]
        val min = calendar[Calendar.MINUTE]
        val cur = ((hour * 60 + min) * 60 * 1000).toLong()
        val diff = st - cur

        val ret = if (diff >= 0) diff else diff + 24 * 3600 * 1000
        return (SystemUtil.systemTime() + ret) / 3600000 * 3600000
    }
}