package com.carota.lib.executor.ui.activity

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
import org.koin.core.component.KoinComponent


abstract class BaseActivity<VB:ViewDataBinding, VM: BaseActivityViewModel<VB>>(
    private val inflate: (LayoutInflater) -> VB,
    private val viewModelClass: Class<VM>
): AppCompatActivity(), KoinComponent {

    val viewModel by lazy {
        val viewModelProvider = ViewModelProvider(this)
        viewModelClass.let { viewModelProvider[it] }
    }

    val binding by lazy { inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.bindData(binding)
        registerLifeCycle()
        setNavigationBarStatusBarTranslucent(this)
        setVariable()
        setContentView(binding.root)
    }

    private fun registerLifeCycle(){
        lifecycle.addObserver(viewModel.lifeCycleObserver)
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

    /**
     * set binding data
     */
    internal abstract fun setVariable()
}