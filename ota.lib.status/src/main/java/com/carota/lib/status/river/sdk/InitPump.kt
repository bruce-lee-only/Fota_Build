package com.carota.lib.status.river.sdk

import com.carota.lib.status.ocean.OceanSdkData

/**
 * this class use for setting value
 * attention: we should not use this class to get ocean value
 */
class InitPump{

    /**
     * 初始化carota sdk 是否结束
     */
    val isInitFinish by lazy { OceanSdkData.INSTANCE.sdkIsInitFinish }

    /**
     * 当前升级是否是resume中断升级
     */
    val isResume by lazy { OceanSdkData.INSTANCE.sdkIsResume }

    /**
     * 当前初始化进行阶段
     */
    val initStep by lazy { OceanSdkData.INSTANCE.sdkInitStep }

    /**
     * Init结果
     */
    val initResult by lazy { OceanSdkData.INSTANCE.initResult }

    /**
     * 升级是否正在进行
     */
    val isUpdateRun by lazy { OceanSdkData.INSTANCE.isUpdateRun }

    companion object{
        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED){ InitPump() }
    }

}