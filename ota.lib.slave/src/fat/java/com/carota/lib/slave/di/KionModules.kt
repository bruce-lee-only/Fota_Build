package com.carota.lib.slave.di

import android.content.Context
import com.carota.lib.slave.SlaveHelper
import org.koin.dsl.module

val slaveModule = module {
    single { SlaveHelper(get<Context>().applicationContext) }
}