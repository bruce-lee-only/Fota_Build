package com.carota.lib.executor.ue.pump

import com.carota.core.ISession
import com.carota.lib.slave.SlaveHandler
import com.carota.lib.status.ocean.OceanSdkData
import com.carota.lib.status.shared.sharedApp

data class PumpDataCheck(private val className: String = PumpDataCheck::class.simpleName ?: "") {

    val checkCareResult: PumpValueObject<Boolean> = PumpValueObject(
        false,
        { className, valueObj -> OceanSdkData.INSTANCE.sdkCheckResult.injectObserver(className, valueObj.property!!.liveData, valueObj::changeBack)},
        { className, valueObj ->  OceanSdkData.INSTANCE.sdkCheckResult.dropObserver(className, valueObj::dropBack)},
        { OceanSdkData.INSTANCE.sdkCheckResult.value },
        true)

    val checkResult: Boolean            get() = checkCareResult.oceanValue()

    val isScheduled: Boolean            get() = sharedApp.scheduleTime > 0

    val isRebootDisplayed: Boolean      get() = !sharedApp.rebootDisplayContent.isNullOrEmpty()

    val isSilentUpgrade: Boolean        get() = OceanSdkData.INSTANCE.sdkUpdateType.value == ISession.MODE_AUTO_INSTALL_SILENT

    private val silentDelayTime: Long   get() = OceanSdkData.INSTANCE.sdkAppointmentTime.value

    val isScheduleEventPass: Boolean    get() = SlaveHandler.isScheduleEventPass(silentDelayTime, -1L)
}