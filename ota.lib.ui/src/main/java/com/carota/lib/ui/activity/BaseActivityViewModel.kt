package com.carota.lib.ui.activity

import android.app.Application
import android.content.Context
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

abstract class BaseActivityViewModel<VB: ViewDataBinding>(application: Application) : AndroidViewModel(
    application
) {
    lateinit var binding: VB
    lateinit var context: Context

    val lifeCycleObserver by lazy {
        object : DefaultLifecycleObserver {
            override fun onCreate(lifecycleOwner: LifecycleOwner)   { onCreate() }

            override fun onDestroy(lifecycleOwner: LifecycleOwner)  { onDestroy() }

            override fun onPause(lifecycleOwner: LifecycleOwner)    { onPause() }

            override fun onResume(lifecycleOwner: LifecycleOwner)   { onResume() }

            override fun onStart(lifecycleOwner: LifecycleOwner)    { onStart() }

            override fun onStop(lifecycleOwner: LifecycleOwner)     { onStop() }
        }
    }

    open fun onStop(){}

    open fun onStart(){}

    open fun onResume(){}

    open fun onPause(){}

    open fun onDestroy(){}

    open fun onCreate(){}

    abstract fun bindData(binding: VB, context: Context)
}