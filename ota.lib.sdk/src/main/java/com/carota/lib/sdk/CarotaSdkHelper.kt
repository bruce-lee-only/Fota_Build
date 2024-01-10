package com.carota.lib.sdk

import android.content.Context
import com.carota.CarotaClient
import com.carota.hmi.CarOtaHmi

internal val carotaSdkHelper by lazy { CarotaSdkHelper() }
class CarotaSdkHelper {

    private var context: Context? = null

    companion object{
        private const val TIMEOUT: Long = 5 * 60 * 1000

        fun carotaSdkInit(context: Context, timeOut: Long = TIMEOUT) = carotaSdkHelper.carotaSdkInit(context, timeOut)

        fun carotaSdkIsUpgradeTriggered(): Boolean = carotaSdkHelper.carotaSdkIsUpgradeTriggered()
    }

    /**
     * 初始化sdk, 调用hmi的init操作
     * @param context Context
     * @param timeOut Long
     */
    fun carotaSdkInit(context: Context, timeOut: Long){
        this.context = context
        CarOtaHmi.init(context, CarotaSdkListener(), timeOut)
    }

    /**
     * 当前是否正在升级
     * @return Boolean
     */
    fun carotaSdkIsUpgradeTriggered(): Boolean {
        return CarotaClient.getClientStatus().isUpgradeTriggered
    }
}