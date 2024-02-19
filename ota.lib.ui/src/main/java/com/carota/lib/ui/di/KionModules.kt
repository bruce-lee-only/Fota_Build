package com.carota.lib.ui.di

import android.content.Context
import com.carota.lib.ui.toast.attention.DialogAttention
import org.koin.dsl.module

val uiModule = module {
    factory { DialogAttention(get<Context>().applicationContext) }
}