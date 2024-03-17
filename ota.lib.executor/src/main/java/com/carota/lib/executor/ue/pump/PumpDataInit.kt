package com.carota.lib.executor.ue.pump

import com.carota.lib.status.ocean.OceanSdkData

data class PumpDataInit(private val className: String = PumpDataInit::class.simpleName ?: "") {
    val initCareIsFinish: PumpValueObject<Boolean> = PumpValueObject(
        false,
        { className, valueObj -> OceanSdkData.INSTANCE.sdkIsInitFinish.injectObserver(className, valueObj.property!!.liveData, valueObj::changeBack)},
        { className, valueObj ->  OceanSdkData.INSTANCE.sdkIsInitFinish.dropObserver(className, valueObj::dropBack)},
        { OceanSdkData.INSTANCE.sdkIsInitFinish.value },
        true)
}