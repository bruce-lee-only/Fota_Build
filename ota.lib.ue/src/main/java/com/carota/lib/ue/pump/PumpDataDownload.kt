package com.carota.lib.ue.pump

import com.carota.core.ISession
import com.carota.lib.status.ocean.OceanSdkData

data class PumpDataDownload(private val className: String = PumpDataCheck::class.simpleName ?: "") {
    val downloadCareResult: PumpValueObject<Boolean> = PumpValueObject(
        false,
        { className, valueObj -> OceanSdkData.INSTANCE.sdkDownloadResult.injectObserver(className, valueObj.property!!.liveData, valueObj::changeBack)},
        { className, valueObj ->  OceanSdkData.INSTANCE.sdkDownloadResult.dropObserver(className, valueObj::dropBack)},
        { OceanSdkData.INSTANCE.sdkDownloadResult.value },
        true)

    val updateType: String      get() = OceanSdkData.INSTANCE.sdkUpdateType.value

    val isFactory: Boolean      get() = updateType == ISession.MODE_AUTO_UPDATE_FACTORY
}