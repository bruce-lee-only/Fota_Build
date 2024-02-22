package com.carota.lib.ue.di

import android.content.Context
import com.carota.lib.ue.NodeCheck
import com.carota.lib.ue.NodeDownload
import com.carota.lib.ue.NodeInit
import com.carota.lib.ue.NodeSelfUpgrade
import org.koin.dsl.module

val ueModule = module {
    factory { NodeInit(get<Context>().applicationContext) }
    factory { NodeCheck() }
    factory { NodeSelfUpgrade() }
    factory { NodeDownload() }
}