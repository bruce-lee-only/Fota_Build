package com.carota.lib.status.river.sdk

import com.carota.lib.status.ocean.OceanSdkData

class RiverDownload {

    val downloadResult by lazy { OceanSdkData.INSTANCE.sdkDownloadResult }

    companion object{
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ RiverDownload() }
    }
}