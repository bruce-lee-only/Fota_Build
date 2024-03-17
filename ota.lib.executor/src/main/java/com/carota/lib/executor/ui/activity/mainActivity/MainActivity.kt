package com.carota.lib.executor.ui.activity.mainActivity

import com.carota.lib.executor.databinding.ActivityMainBinding
import com.carota.lib.executor.ui.activity.BaseActivity
import com.carota.lib.executor.ui.activity.care.MainActivityCare
import com.carota.lib.executor.ui.uiHelper.DebounceProxy
import com.carota.lib.executor.ui.uiHelper.SingleClickImpl
import com.carota.lib.executor.ui.uiHelper.SingleInterface

class MainActivity: BaseActivity<ActivityMainBinding, MainActivityViewModel>(
    ActivityMainBinding::inflate,
    MainActivityViewModel::class.java,
){
    private lateinit var singleClickImpl: SingleInterface
    private lateinit var singleClick: SingleInterface

    override fun setVariable() {
        binding.mainActivityView = this
        binding.mainActivityViewModel = viewModel
        binding.mainCare = MainActivityCare()

        singleClickImpl = SingleClickImpl(viewModel)
        singleClick     = DebounceProxy.bind(singleClickImpl)
    }

    fun onPlaySoundClicked(){

    }

    fun onReleaseNoteClick(){

    }

    fun onHideViewClicked(){

    }

    fun onDetailClicked(){

    }

    fun onUpgradeClicked(){
        singleClick.onUpdateClick()
    }

    fun onScheduleClicked(){
        singleClick.onScheduleClick()
    }
}
