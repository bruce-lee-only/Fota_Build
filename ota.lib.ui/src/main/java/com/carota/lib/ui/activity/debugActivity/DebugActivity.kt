package com.carota.lib.ui.activity.debugActivity

import com.carota.lib.ui.activity.BaseActivity
import com.carota.lib.ui.databinding.ActivityDebugBinding

class DebugActivity: BaseActivity<ActivityDebugBinding, DebugActivityViewModel>(
    ActivityDebugBinding::inflate,
    DebugActivityViewModel::class.java,
) {
}