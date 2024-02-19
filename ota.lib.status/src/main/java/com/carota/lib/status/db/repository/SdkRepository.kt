package com.carota.lib.status.db.repository

import com.carota.lib.status.db.AppDatabase
import com.carota.lib.status.db.dao.SdkDao
import com.carota.lib.status.db.entity.SdkEntity

class SdkRepository {

    private val sdkDao: SdkDao = AppDatabase.getDatabase().sdkDao()

    @Synchronized
    fun insertSdk(sdkEntity: SdkEntity){
        sdkDao.insertSdk(sdkEntity)
    }

    @Synchronized
    fun updateSdk(sdkEntity: SdkEntity){
        if (isSdkEmpty()){
            insertSdk(sdkEntity)
        }else{
            sdkDao.updateSdk(sdkEntity)
        }
    }

    @Synchronized
    fun querySdk(): SdkEntity?{
        return sdkDao.querySdk()
    }

    @Synchronized
    fun isSdkEmpty(): Boolean {
        return querySdk() == null
    }

}