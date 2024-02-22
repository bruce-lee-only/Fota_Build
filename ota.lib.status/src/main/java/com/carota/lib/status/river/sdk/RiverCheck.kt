package com.carota.lib.status.river.sdk

import com.carota.lib.status.ocean.OceanSdkData

/**
 * this class use for setting value
 * attention: we should not use this class to get ocean value
 */
class RiverCheck {
    /**
     * sdk check step
     */
    val checkStep by lazy { OceanSdkData.INSTANCE.sdkCheckStep }

    /**
     * check Result
     */
    val checkResult by lazy { OceanSdkData.INSTANCE.sdkCheckResult }

    /**
     * update type
     */
    val updateType by lazy { OceanSdkData.INSTANCE.sdkUpdateType }

    /**
     * silent schedule time
     */
    val appointmentTime by lazy { OceanSdkData.INSTANCE.sdkAppointmentTime }

    companion object{
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ RiverCheck() }
    }
}