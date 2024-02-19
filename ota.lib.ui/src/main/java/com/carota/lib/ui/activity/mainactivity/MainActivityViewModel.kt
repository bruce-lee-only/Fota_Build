package com.carota.lib.ui.activity.mainactivity

import android.app.Application
import com.carota.lib.ui.activity.BaseActivityViewModel
import com.carota.lib.ui.databinding.ActivityMainBinding

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
}