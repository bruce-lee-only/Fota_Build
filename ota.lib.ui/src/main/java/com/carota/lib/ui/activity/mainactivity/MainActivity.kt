package com.carota.lib.ui.activity.mainactivity

import com.carota.lib.ui.BR
import com.carota.lib.ui.activity.BaseActivity
import com.carota.lib.ui.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity: BaseActivity<ActivityMainBinding, MainActivityViewModel>(
    ActivityMainBinding::inflate,
    MainActivityViewModel::class.java
) {
    override fun initActivity(
        viewModel: MainActivityViewModel?,
    ){
        binding.setVariable(BR.main_activity_view, this)
        binding.setVariable(BR.main_activity_viewModel, viewModel)
    }

}

