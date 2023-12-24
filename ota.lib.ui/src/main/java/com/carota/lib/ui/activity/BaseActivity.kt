package com.carota.lib.ui.activity

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.carota.lib.ui.BR


abstract class BaseActivity<VB:ViewDataBinding, VM:BaseActivityViewModel<VB>>(
    private val inflate: (LayoutInflater) -> VB,
    private val viewModelClass: Class<VM>?
): AppCompatActivity() {

    lateinit var binding: VB

    private val viewModel by lazy {
        val viewModelProvider = ViewModelProvider(this)
        viewModelClass?.let { viewModelProvider[it] }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        initActivity(viewModel)
        viewModel?.bindData(binding, this)
        registerLifeCycle()
        setNavigationBarStatusBarTranslucent(this)
        setContentView(binding.root)
    }

    private fun registerLifeCycle(){
        viewModel?.let { lifecycle.addObserver(it.lifeCycleObserver) }
    }

    /**
     * 隐藏状态栏，导航栏
     */
    @Suppress("DEPRECATION")
    fun setNavigationBarStatusBarTranslucent(activity: Activity) {
        val window: Window = activity.window
        val decorView: View = window.decorView
        val option = (View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
        decorView.systemUiVisibility = option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        window.navigationBarColor = Color.TRANSPARENT
        window.statusBarColor = Color.TRANSPARENT
    }

    abstract fun initActivity(viewModel: VM?)
}