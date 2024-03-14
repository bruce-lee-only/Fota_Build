package com.carota.fota_build

import android.os.Build
import androidx.annotation.RequiresApi
import com.carota.hmi.CarOtaHmi
import com.carota.hmi.type.UpgradeType

abstract class MainApplication: BaseApplication() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate() {
        super.onCreate()
    }

    abstract val policyMap: HashMap<UpgradeType, CarOtaHmi.Policy>
}