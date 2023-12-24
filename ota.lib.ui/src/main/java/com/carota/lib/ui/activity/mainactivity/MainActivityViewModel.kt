package com.carota.lib.ui.activity.mainactivity

import android.app.Application
import android.content.Context
import com.carota.lib.ui.activity.BaseActivityViewModel
import com.carota.lib.ui.databinding.ActivityMainBinding

class MainActivityViewModel(application: Application) : BaseActivityViewModel<ActivityMainBinding>(
    application
) {

    override fun bindData(binding: ActivityMainBinding, context: Context) {
        this.binding = binding
        this.context = context
    }

    override fun onStop() {

    }

    override fun onStart() {
        println("onStart()")
    }

    override fun onResume() {
        println("onResume()")
    }
}