package com.carota.lib.status.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

//todo: defined a custom table name, otherwise named with the class name
@Entity(tableName = "sdk")
data class SdkEntity(
    @PrimaryKey(autoGenerate = true)
    var id:                 Int = 1,
    var sdkIsResume:        Boolean = false,
    var sdkIsInitFinish:    Boolean = false,
    var sdkInitResult:      Boolean = false,
    var sdkIsUpdateRun:     Boolean = false,
    var sdkInitStep:        String? = null,
    var sdkCheckStep:       String? = null,
    var sdkSelfUpgradeRet:  Int     = 1,
    var sdkCheckResult:     Boolean = false,
    var sdkDownloadResult:  Boolean = false,
    var sdkUpdateType:      String? = null,
    var sdkAppointmentTime: Long    = -1L

)
