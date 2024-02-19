package com.carota.lib.ui.activity.mainactivity

import com.carota.lib.ui.activity.BaseActivity
import com.carota.lib.ui.databinding.ActivityMainBinding

class MainActivity: BaseActivity<ActivityMainBinding, MainActivityViewModel>(
    ActivityMainBinding::inflate,
    MainActivityViewModel::class.java,
) {
}

