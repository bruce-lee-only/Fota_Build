package com.carota.lib.ui.toast.base

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.carota.lib.common.uitls.Logger
import com.carota.lib.ui.toast.DialogDisclaimerViewModel

abstract class BaseDialog <VB:ViewDataBinding, VM: BaseDialogViewModel<VB>>(
    private val inflate: (LayoutInflater) -> VB,
    private val viewModelClass: Class<DialogDisclaimerViewModel<*>>,
    private val context: Context){

    val viewModel by lazy {
        Factory(context as Application).create(viewModelClass)
    }

    val binding by lazy {
        inflate(LayoutInflater.from(context))
    }

    open fun initData(){
        setVariable()
    }

    open fun onShow() {}

    open fun onDismiss() {}

    abstract fun setVariable()

    class Factory(val application: Application) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            try {
                return modelClass.getConstructor(Application::class.java).newInstance(application)
            } catch (e: Exception) {
                Logger.error("create view model exception: $e")
            }
            return super.create(modelClass)
        }
    }
}