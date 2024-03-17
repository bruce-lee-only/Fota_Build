package com.carota.lib.executor.ue.pump

import com.carota.lib.sdk.CarotaSdkHelper
import com.carota.lib.status.ocean.OceanSdkData
import com.carota.lib.status.shared.sharedApp

data class PumpDataSelfUpgrade(private val className: String = PumpDataSelfUpgrade::class.simpleName ?: "") {

    val isUpgradeRunning: Boolean       get() = OceanSdkData.INSTANCE.sdkIsUpdateRun.value

    val isScheduled: Boolean            get() = sharedApp.scheduleTime > 0

    val isRebootDisplayed: Boolean      get() = !sharedApp.rebootDisplayContent.isNullOrEmpty()

    val vehicleVin: String              get() = CarotaSdkHelper.carotaSdkGetVin()
}