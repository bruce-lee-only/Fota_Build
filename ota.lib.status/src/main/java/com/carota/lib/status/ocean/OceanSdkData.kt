package com.carota.lib.status.ocean

import com.carota.lib.status.db.entity.SdkEntity
import com.carota.lib.status.db.repository.SdkRepository
import org.koin.core.component.KoinComponent

class OceanSdkData: KoinComponent {

    private val sdkRepository = SdkRepository()
    private val sdkEntity get() = sdkRepository.querySdk() ?: SdkEntity()

    /**
     * 是否断电续升级
     * 取值时只取内存值，不取数据库值
     */
    var sdkIsResume: OceanValueObject<Boolean> = OceanValueObject(
        false,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkIsResume = it }) }){ it.value }


    /**
     * sdk初始化是否结束
     * 取值时只取内存值，不取数据库值
     */
    var sdkIsInitFinish: OceanValueObject<Boolean> = OceanValueObject(
        false,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkIsInitFinish = it })}){ it.value }

    /**
     * sdk init step
     * return value: from memory
     */
    var sdkInitStep: OceanValueObject<String> = OceanValueObject(
        SDK_INIT_STEP_DEFAULT,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkInitStep = it }) }){ it.value }

    /**
     * sdk init result
     * return value: from memory
     */
    var sdkInitResult: OceanValueObject<Boolean> = OceanValueObject(
        false,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkInitResult = it }) }){ it.value }

    /**
     * is upgrade running
     * return value: from memory
     */
    var sdkIsUpdateRun: OceanValueObject<Boolean> = OceanValueObject(
        false,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkIsUpdateRun = it }) }){ it.value }

    companion object{
        const val SDK_INIT_STEP_START   : String = "started"

        const val SDK_INIT_STEP_FINISH  : String = "finished"

        const val SDK_INIT_STEP_DEFAULT : String = "default"

        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { OceanSdkData() }
    }

    private fun updateDb(entity: SdkEntity){
        sdkRepository.updateSdk(entity)
    }
}
