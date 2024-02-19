package com.carota.lib.status.ocean

import com.carota.lib.status.shared.SharedManager.Statical.VEHICLE_MODE_REAL
import com.carota.lib.status.shared.sharedApp

class OceanSystemData{
    /**
     * 当前是否是debug模式
     */
    val systemIsDebugMode: Boolean          get() = false

    /**
     * 当前是否在工程模式
     */
    val systemIsEngineMode: Boolean         get() = false

    /**
     * is apk run with real vehicle
     */
    val systemIsRealVehicle: Boolean        get() = sharedApp.sysRunMode == VEHICLE_MODE_REAL
}
