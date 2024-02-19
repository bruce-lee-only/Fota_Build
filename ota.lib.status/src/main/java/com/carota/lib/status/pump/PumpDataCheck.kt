package com.carota.lib.status.pump

import com.carota.lib.status.ocean.OceanSdkData

class PumpDataCheck {

    val checkCareIsFinish: PumpValueObject<Boolean> = PumpValueObject(
        false,
        { className, valueObj -> OceanSdkData.INSTANCE.sdkIsInitFinish.injectObserver(className, valueObj.property!!.liveData, valueObj::changeBack)},
        { className, valueObj ->  OceanSdkData.INSTANCE.sdkIsInitFinish.dropObserver(className, valueObj::dropBack)},
        { OceanSdkData.INSTANCE.sdkIsInitFinish.value },
        true)
}