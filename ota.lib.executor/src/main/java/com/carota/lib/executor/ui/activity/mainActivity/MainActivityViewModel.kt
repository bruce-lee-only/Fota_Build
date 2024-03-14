package com.carota.lib.executor.ui.activity.mainActivity

import android.app.Application
import com.carota.lib.executor.databinding.ActivityMainBinding
import com.carota.lib.executor.ui.activity.BaseActivityViewModel

class MainActivityViewModel(application: Application) : BaseActivityViewModel<ActivityMainBinding>(
    application
) {

    override fun onStop() {
        this.context
    }

    override fun onStart() {
        println("onStart()")
    }

    override fun onResume() {
        println("onResume()")
    }

    fun onUpgradeClicked(){

    }

    fun onScheduleClicked(){

    }
}