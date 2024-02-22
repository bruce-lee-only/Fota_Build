package com.carota.lib.sdk

import android.content.Context
import com.carota.CarotaClient
import com.carota.hmi.CarOtaHmi
import com.carota.hmi.UpgradeType
import com.carota.lib.common.uitls.Logger
import com.carota.lib.status.ocean.OceanSdkData
import com.carota.lib.status.river.sdk.RiverSelfUpgrade
import com.carota.sota.IApplyUpgradeCallback
import com.carota.sota.ICheckResultCallback
import com.carota.sota.SoftwareManager
import com.carota.sota.store.AppData
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean

internal val carotaSdkHelper by lazy { CarotaSdkHelper() }
class CarotaSdkHelper {

    private var appContext: Context? = null

    companion object{
        private const val TIMEOUT: Long = 5 * 60 * 1000

        fun carotaSdkInit(context: Context, timeOut: Long = TIMEOUT) = carotaSdkHelper.carotaSdkInit(context, timeOut)

        //todo: normal task check
        fun carotaSdkNormalCheck() = carotaSdkHelper.carotaSdkCheck()

        fun carotaSdkIsUpgradeTriggered(): Boolean = carotaSdkHelper.carotaSdkIsUpgradeTriggered()

        fun carotaSdkGetVin(): String = carotaSdkHelper.carotaSdkGetVin()

        fun carotaSelfUpgrade(vin: String = carotaSdkGetVin(), block:(Int)->Unit) = carotaSdkHelper.carotaSelfUpgrade(vin, block)

        fun carotaSdkDownloadUpgradeNow() = carotaSdkHelper.carotaDownload(type = UpgradeType.DEFULT)
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

    /**
     * get vehicle vin code
     * @return String
     */
    fun carotaSdkGetVin(): String {
        //fixme: need add query vehicle info math to sdk
        return ""
    }

    /**
     * do apk self upgrade
     * @param vin String: vehicle vin code
     * @param block Function0<UInt>: call back function
     */
    fun carotaSelfUpgrade(vin: String, block:(Int)->Unit) {
        if (appContext == null){
            Logger.error("carota sdk do self upgrade fail, app context is null, check init")
            block(OceanSdkData.SDK_SELF_UPGRADE_NO_NEED)
            return
        }else{
            val countDownLatch = CountDownLatch(1)
            val needUpdateApp = AtomicBoolean(false)
            var appDataUse: AppData? = null

            Logger.info("SoftwareManager start check upgrade task")
            SoftwareManager.checkUpgrade(appContext, vin, null, null, object : ICheckResultCallback{
                override fun onConnected(code: Int) {
                    Logger.info("SoftwareManager checkUpgrade onConnected code: $code")
                }

                override fun onResult(appData: AppData?) {
                    Logger.info("SoftwareManager checkUpgrade onResult AppData: $appData")
                    appData?.let {
                        if (it.appInfoCount > 0){
                            needUpdateApp.set(true)
                            appDataUse = it
                        }
                    }?: run { Logger.error("SoftwareManager checkUpgrade fail, appdata is null") }
                    countDownLatch.countDown()
                }

                override fun onError(errorCode: Int, msg: String?) {
                    Logger.info("SoftwareManager checkUpgrade onError errorCode:$errorCode, message:$msg")
                    countDownLatch.countDown()
                }
            })

            countDownLatch.await()
            Thread.sleep(2000)

            Logger.info("self upgrade needUpdateApp: ${needUpdateApp.get()}")
            if (needUpdateApp.get() && appDataUse != null){
                SoftwareManager.applyUpgrade(appContext, appDataUse, object : IApplyUpgradeCallback{
                    override fun onDownloading(appData: AppData?, index: Int, pg: Int) {
                        Logger.info("self upgrade applyUpgrade onDownloading index = $index / pg = $pg")
                    }

                    override fun onInstalling(appData: AppData?, index: Int, pg: Int) {
                        Logger.info("self upgrade applyUpgrade onDownloading index = $index / pg = $pg")
                    }

                    override fun onFinished(appData: AppData?, index: Int) {
                        Logger.info("self upgrade applyUpgrade onFinished index = $index")
                        OceanSdkData.SDK_SELF_UPGRADE_SUCCESS.let{
                            RiverSelfUpgrade.INSTANCE.selfUpgradeResult.value = it
                            block(it)
                        }
                    }

                    override fun onError(appData: AppData?, index: Int, errorCode: Int) {
                        Logger.error("self upgrade applyUpgrade onError index = $index / errorCode = $errorCode")
                        OceanSdkData.SDK_SELF_UPGRADE_FAILED.let{
                            RiverSelfUpgrade.INSTANCE.selfUpgradeResult.value = it
                            block(it)
                        }
                    }
                })
            }else{
                block(OceanSdkData.SDK_SELF_UPGRADE_NO_NEED)
            }
        }
    }

    /**
     * sdk download task
     * @param type UpgradeType
     */
    fun carotaDownload(type: UpgradeType = UpgradeType.DEFULT){
        Logger.drop("download type: $type")
        CarOtaHmi.download(type, CarotaSdkListenerHandler().mIDownload)
    }
}