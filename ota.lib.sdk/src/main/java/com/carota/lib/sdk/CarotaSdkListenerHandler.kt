package com.carota.lib.sdk

import com.carota.hmi.UpgradeType
import com.carota.hmi.callback.ICall
import com.carota.hmi.status.IStatus
import com.momock.util.Logger

class CarotaSdkListenerHandler {
    val mICheck : ICall     get() = object : ICall{
            override fun onStart(upgradeType: UpgradeType?) {
                Logger.info("carota sdk check onStart")
            }

            override fun onError(upgradeType: UpgradeType?, error: Int) {
                Logger.info("carota sdk check onError")
            }

            override fun onEnd(upgradeType: UpgradeType?, success: Boolean, status: IStatus?) {
                Logger.info("carota sdk check onEnd")
            }
        }
}