package com.carota.lib.executor.ui.activity.debugActivity

import com.carota.lib.executor.databinding.ActivityDebugBinding
import com.carota.lib.executor.ue.node.NodeStorageBattery
import com.carota.lib.executor.ue.node.NodeNotify
import com.carota.lib.executor.ue.node.NodePowerBattery
import com.carota.lib.executor.ui.activity.BaseActivity
import org.koin.core.component.inject

class DebugActivity: BaseActivity<ActivityDebugBinding, DebugActivityViewModel>(
    ActivityDebugBinding::inflate,
    DebugActivityViewModel::class.java,
){

    override fun setVariable() {
        binding.debugActivityView = this
        binding.debugActivityViewModel = viewModel
    }

    fun onNewTaskClicked(){
        val node: NodeNotify by inject()
        node.run()
    }

    fun onDisclaimerClicked(){

    }

    fun onSetScheduleTimeClicked(){

    }

    fun onDisplayLowStorageBatteryDialog(){
        val node: NodeStorageBattery by inject()
        node.run()
    }

    fun onDisplayLowPowerBatteryDialog(){
        val node: NodePowerBattery by inject()
        node.run()
    }
}