package com.carota.lib.executor.di

import android.content.Context
import com.carota.hmi.CarOtaHmi
import com.carota.hmi.type.UpgradeType
import com.carota.lib.executor.ue.node.NodeStorageBattery
import com.carota.lib.executor.ue.node.NodeCheck
import com.carota.lib.executor.ue.node.NodeDownload
import com.carota.lib.executor.ue.node.NodeInit
import com.carota.lib.executor.ue.node.NodeMainActivity
import com.carota.lib.executor.ue.node.NodeNotify
import com.carota.lib.executor.ue.node.NodePowerBattery
import com.carota.lib.executor.ue.node.NodePowerOff
import com.carota.lib.executor.ue.node.NodeSelfUpgrade
import com.carota.lib.executor.ui.dialog.attention.DialogAttention
import com.carota.lib.executor.ui.dialog.battery.DialogLowBattery
import com.carota.lib.executor.ui.dialog.notify.DialogNotify
import org.koin.core.module.Module
import org.koin.dsl.module

val uiModule = module {
    factory { DialogAttention(get<Context>().applicationContext) }
    factory { DialogNotify(get<Context>().applicationContext) }
    factory { DialogLowBattery(get<Context>().applicationContext) }
}

fun ueModule(map: HashMap<UpgradeType, CarOtaHmi.Policy>): Module{
    return module {
        factory { NodeInit(get<Context>().applicationContext, map) }
        factory { NodeCheck() }
        factory { NodeSelfUpgrade() }
        factory { NodeDownload() }
        factory { NodeNotify() }
        factory { NodePowerOff() }
        factory { NodeStorageBattery() }
        factory { NodePowerBattery() }
        factory { NodeMainActivity(get<Context>().applicationContext) }
    }
}