package com.carota.lib.status.ocean

data class OceanSdkData(private val id: Int){

    private val default : String = "default"

    /**
     * 是否断电续升级
     * 取值时只取内存值，不取数据库值
     */
    var sdkIsResume: OceanValueObject<Boolean> = OceanValueObject(false){ true }


    /**
     * sdk初始化是否结束
     * 取值时只取内存值，不取数据库值
     */
    var sdkIsInitFinish: OceanValueObject<Boolean> = OceanValueObject(false){ true }

    /**
     * sdk init step
     * return value: from memory
     */
    var sdkInitStep: OceanValueObject<String> = OceanValueObject(INIT_STEP_DEFAULT){ INIT_STEP_DEFAULT }

    /**
     * sdk init result
     * return value: from memory
     */
    var initResult: OceanValueObject<Boolean> = OceanValueObject(false){ false }

    /**
     * is upgrade running
     * return value: from memory
     */
    var isUpdateRun: OceanValueObject<Boolean> = OceanValueObject(false){ false }

    companion object{
        const val INIT_STEP_START   : String = "started"

        const val INIT_STEP_FINISH  : String = "finished"

        const val INIT_STEP_DEFAULT : String = "default"

        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { OceanSdkData(1) }
    }
}
