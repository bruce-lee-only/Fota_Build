package com.carota.lib.ui.activity.engineActivity

import com.carota.lib.ui.activity.BaseActivity
import com.carota.lib.ui.databinding.ActivityEngineBinding

class EngineActivity: BaseActivity<ActivityEngineBinding, EngineActivityViewModel>(
    ActivityEngineBinding::inflate,
    EngineActivityViewModel::class.java,
) {
}