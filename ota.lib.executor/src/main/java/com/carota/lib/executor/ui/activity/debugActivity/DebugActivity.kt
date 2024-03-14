package com.carota.lib.executor.ui.activity.debugActivity

import android.os.Bundle
import android.view.View
import android.widget.Button
import com.carota.lib.common.uitls.Logger
import com.carota.lib.executor.R
import com.carota.lib.executor.databinding.ActivityDebugBinding
import com.carota.lib.executor.generated.callback.OnClickListener
import com.carota.lib.executor.ue.node.NodeNotify
import com.carota.lib.executor.ue.node.NodePowerOff
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
        Logger.drop("开始显示弹框")
        val node: NodeNotify by inject()
        node.run()
    }

    fun onDisclaimerClicked(){
        Logger.drop("开始显示弹框")
    }

    fun onSetScheduleTimeClicked(){
        Logger.drop("开始显示弹框")
    }
}