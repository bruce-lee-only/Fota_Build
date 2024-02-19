package com.carota.lib.status.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.carota.lib.common.uitls.Logger
import com.carota.lib.status.db.dao.SdkDao
import com.carota.lib.status.db.entity.SdkEntity


@Database(version = 1, entities = [SdkEntity::class], exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sdkDao(): SdkDao

    companion object {

        private var instance        : AppDatabase?  = null
        private const val dbName    : String        = "app_database"

        //kotlin的正规双重校验锁写法
        private fun getDatabaseSingleton(context: Context): AppDatabase =
            instance ?: synchronized(AppDatabase::class.java) {
                Logger.info("Room build date base $dbName")
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    dbName
                )
                    .enableMultiInstanceInvalidation()//todo: 一个进程中使共享数据库文件失效，使得其他进程中 AppDatabase 的实例失效。
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }

        @Synchronized
        internal fun getDatabase(context: Context? = null): AppDatabase {
            if (context == null && instance == null) { throw IllegalArgumentException("you should invoke setDatabase function before use database or run with a context to create it!") }
            if (instance == null) { getDatabaseSingleton(context!!) }
            return instance!!
        }

        //todo: we should invoke this function before use database
        @Synchronized
        fun setDatabase(context: Context){
            getDatabaseSingleton(context)
        }
    }
}