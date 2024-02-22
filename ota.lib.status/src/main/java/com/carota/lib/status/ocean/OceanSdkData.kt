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
        OceanValueObject.DEFAULT_BOOLEAN,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkIsResume = it }) }){ it.value }


    /**
     * sdk初始化是否结束
     * 取值时只取内存值，不取数据库值
     */
    var sdkIsInitFinish: OceanValueObject<Boolean> = OceanValueObject(
        OceanValueObject.DEFAULT_BOOLEAN,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkIsInitFinish = it })}){ it.value }

    /**
     * sdk init step
     * return value: from memory
     */
    var sdkInitStep: OceanValueObject<String> = OceanValueObject(
        SDK_STEP_DEFAULT,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkInitStep = it }) }){ it.value }

    /**
     * sdk init result
     * return value: from memory
     */
    var sdkInitResult: OceanValueObject<Boolean> = OceanValueObject(
        OceanValueObject.DEFAULT_BOOLEAN,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkInitResult = it }) }){ it.value }

    /**
     * is upgrade running
     * return value: from memory
     */
    var sdkIsUpdateRun: OceanValueObject<Boolean> = OceanValueObject(
        OceanValueObject.DEFAULT_BOOLEAN,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkIsUpdateRun = it }) }){ it.value }

    /**
     * sdk check step
     * return value: from memory
     */
    var sdkCheckStep: OceanValueObject<String> = OceanValueObject(
        SDK_STEP_DEFAULT,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkInitStep = it }) }){ it.value }

    /**
     * sdk check task result
     */
    var sdkCheckResult: OceanValueObject<Boolean> = OceanValueObject(
        OceanValueObject.DEFAULT_BOOLEAN,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkCheckResult = it }) }){ it.value }

    /**
     * sdk self upgrade result
     * return value: from memory
     */
    var sdkSelfUpgradeResult: OceanValueObject<Int> = OceanValueObject(
        SDK_SELF_UPGRADE_NO_NEED,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkSelfUpgradeRet = it }) }){ it.value }

    /**
     * sdk check task result
     * return value: from memory
     */
    var sdkDownloadResult: OceanValueObject<Boolean> = OceanValueObject(
        OceanValueObject.DEFAULT_BOOLEAN,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkDownloadResult = it }) }){ it.value }

    /**
     * ota update type
     * return value: from memory
     */
    var sdkUpdateType: OceanValueObject<String> = OceanValueObject(
        OceanValueObject.DEFAULT_STRING,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkUpdateType = it }) }){ it.value }

    /**
     * silent schedule time from server
     * return value: from memory
     */
    var sdkAppointmentTime: OceanValueObject<Long> = OceanValueObject(
        OceanValueObject.DEFAULT_LONG,
        isPostEarly = false,
        { updateDb(sdkEntity.apply { this.sdkAppointmentTime = it }) }){ it.value }


    companion object{
        const val SDK_STEP_START    : String = "started"

        const val SDK_STEP_FINISH   : String = "finished"

        const val SDK_STEP_DEFAULT  : String = "default"

        const val SDK_STEP_ERROR    : String = "error"

        const val SDK_SELF_UPGRADE_SUCCESS  : Int  = 0
        const val SDK_SELF_UPGRADE_NO_NEED  : Int  = 1
        const val SDK_SELF_UPGRADE_FAILED   : Int  = 2

        val INSTANCE by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) { OceanSdkData() }
    }

    private fun updateDb(entity: SdkEntity){
        sdkRepository.updateSdk(entity)
    }
}
