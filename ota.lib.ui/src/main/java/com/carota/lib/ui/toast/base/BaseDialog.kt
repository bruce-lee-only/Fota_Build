package com.carota.lib.ui.toast.base

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.carota.lib.common.uitls.Logger
import com.carota.lib.ui.toast.base.basedialog.ToastManager
import com.carota.lib.ui.toast.base.basedialog.showDialog

abstract class BaseDialog <VB:ViewDataBinding, VM: BaseDialogViewModel<VB, T>, T: BaseCare>(
    private val inflate: (LayoutInflater) -> VB,
    private val viewModelClass: Class<VM>,
    private val context: Context): IDialog{

     internal open val viewModel by lazy {
        Factory(context as Application).create(viewModelClass)
    }

    internal open val binding by lazy {
        inflate(LayoutInflater.from(context))
    }

    private var layoutId           : Int = 0
    private var toastLevel         : Int = Int.MIN_VALUE
    private var params             : WindowManager.LayoutParams? = null
    private var dismissListener    : (() -> Unit)? = null
    private var displayListener    : ((View) -> Unit)? = null

    internal abstract fun setVariable()
    /**
     * do nothing about layout params, use default
     */
    internal abstract fun setLayoutParams()
    /**
     * run without dismiss listener
     * use this when you do not care about dialog close action
     */
    internal abstract fun setDismissListener()
    /**
     * run without show listener
     * use this when you do not care about dialog show action
     */
    internal abstract fun setShowListener()

    internal open fun initData(){
        viewModel.binding = this.binding
        setVariable()
        setLayoutParams()
        setShowListener()
        setDismissListener()
    }

    internal fun setLayoutParams(params: WindowManager.LayoutParams?){
        this.params = params
    }

    internal fun setDismissListener(listener: (() -> Unit)?) {
        this.dismissListener = listener
    }

    internal fun setShowListener(listener: ((View) -> Unit)?) {
        this.displayListener = listener
    }

    /**
     * onShow dialog with do nothing
     * override this function if we you to do some extra
     */
    override fun doOnShow() {
        showDialog(
            context         = context,
            layoutId        = binding.root.id,
            view            = binding.root,
            toastLevel      = toastLevel,
            params          = params,
            dismissListener = dismissListener,
            showListener    = displayListener)
    }

    /**
     * close dialog with do nothing
     * override this function if we you to do some extra
     */
    override fun doOnDismiss() { ToastManager.get().dismiss() }

    internal class Factory(val application: Application) : ViewModelProvider.NewInstanceFactory() {

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