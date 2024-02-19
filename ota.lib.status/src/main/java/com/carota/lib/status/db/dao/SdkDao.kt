package com.carota.lib.status.db.dao

import androidx.room.*
import com.carota.lib.status.db.entity.SdkEntity

@Dao
interface SdkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSdk(sdkEntity: SdkEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateSdk(sdkEntity: SdkEntity)

    @Query("select * from sdk")
    fun querySdk():SdkEntity?
}