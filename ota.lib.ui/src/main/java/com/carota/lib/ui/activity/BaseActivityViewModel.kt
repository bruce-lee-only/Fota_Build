package com.carota.lib.ui.activity

import android.app.Application
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

abstract class BaseActivityViewModel<VB: ViewDataBinding>(val context: Application)
    : AndroidViewModel(context), IViewModel<VB> {

    lateinit var binding: VB

    val lifeCycleObserver by lazy {
        object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner)   { onCreate() }
            override fun onDestroy(owner: LifecycleOwner)  { onDestroy() }
            override fun onPause(owner: LifecycleOwner)    { onPause() }
            override fun onResume(owner: LifecycleOwner)   { onResume() }
            override fun onStart(owner: LifecycleOwner)    { onStart() }
            override fun onStop(owner: LifecycleOwner)     { onStop() }
        }
    }

    open fun onStop(){}
    open fun onStart(){}
    open fun onResume(){}
    open fun onPause(){}
    open fun onDestroy(){}
    open fun onCreate(){}

    override fun bindData(binding: VB){
        this.binding = binding
    }
}