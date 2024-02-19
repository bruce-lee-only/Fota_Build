package com.carota.lib.sdk

import android.content.Context
import com.carota.CarotaClient
import com.carota.hmi.CarOtaHmi
import com.carota.hmi.UpgradeType

internal val carotaSdkHelper by lazy { CarotaSdkHelper() }
class CarotaSdkHelper {

    private var appContext: Context? = null

    companion object{
        private const val TIMEOUT: Long = 5 * 60 * 1000

        fun carotaSdkInit(context: Context, timeOut: Long = TIMEOUT) = carotaSdkHelper.carotaSdkInit(context, timeOut)

        //todo: normal check
        fun carotaSdkNormalCheck() = carotaSdkHelper.carotaSdkCheck()

        fun carotaSdkIsUpgradeTriggered(): Boolean = carotaSdkHelper.carotaSdkIsUpgradeTriggered()
    }

    /**
     * 初始化sdk, 调用hmi的init操作
     * @param context Context
     * @param timeOut Long
     */
    fun carotaSdkInit(context: Context, timeOut: Long){
        this.appContext = context
        CarOtaHmi.init(context, CarotaSdkListener(), timeOut)
    }

    fun carotaSdkCheck(type: UpgradeType = UpgradeType.DEFULT){
        CarOtaHmi.check(type, CarotaSdkListener().check())
    }

    /**
     * 当前是否正在升级
     * @return Boolean
     */
    fun carotaSdkIsUpgradeTriggered(): Boolean {
        return CarotaClient.getClientStatus().isUpgradeTriggered
    }
}