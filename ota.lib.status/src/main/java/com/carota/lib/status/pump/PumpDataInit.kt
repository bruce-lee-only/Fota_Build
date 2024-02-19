package com.carota.lib.status.pump

import com.carota.lib.status.ocean.OceanSdkData
import com.carota.lib.status.shared.sharedApp

class PumpDataInit {
    val initCareIsFinish: PumpValueObject<Boolean> = PumpValueObject(
        false,
        { className, valueObj -> OceanSdkData.INSTANCE.sdkIsInitFinish.injectObserver(className, valueObj.property!!.liveData, valueObj::changeBack)},
        { className, valueObj ->  OceanSdkData.INSTANCE.sdkIsInitFinish.dropObserver(className, valueObj::dropBack)},
        { OceanSdkData.INSTANCE.sdkIsInitFinish.value },
        true)

    val isResume: Boolean       get() = OceanSdkData.INSTANCE.sdkIsResume.value

    val isReboot: Boolean       get() = !sharedApp.rebootDisplayContent.isNullOrEmpty()

    operator fun component1() = initCareIsFinish.oceanValue()
    operator fun component2() = OceanSdkData.INSTANCE.sdkIsResume.value
    operator fun component3() = !sharedApp.rebootDisplayContent.isNullOrEmpty()
}