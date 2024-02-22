package com.carota.lib.status.river.sdk

import com.carota.lib.status.ocean.OceanSdkData

class RiverSelfUpgrade {
    val selfUpgradeResult by lazy { OceanSdkData.INSTANCE.sdkSelfUpgradeResult }

    companion object{
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ RiverSelfUpgrade() }
    }
}