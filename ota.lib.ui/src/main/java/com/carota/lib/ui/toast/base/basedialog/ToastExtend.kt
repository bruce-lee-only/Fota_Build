package com.carota.lib.ui.toast.base.basedialog

import android.content.Context
import android.view.View
import android.view.WindowManager
import androidx.databinding.ViewDataBinding

fun showDialog(
    context: Context,
    layoutId: Int,
    view: View? = null,
    toastLevel: Int = Int.MIN_VALUE,
    params: WindowManager.LayoutParams? = null,
    binding: ViewDataBinding? = null,
    dismissListener: (() -> Unit)? = null,
    showListener: ((View) -> Unit)? = null
) {
    ToastManager.get().show(
        CarOtaToast.Builder(context)
            .setLevel(toastLevel)
            .setParams(params)
            .setView(layoutId, view)
            .setShowListener(showListener)
            .setBinding(binding)
            .setDismissListener(dismissListener)
    )
}

fun isShowToast() = ToastManager.get().isShow

fun dismissToast() = closeToast()

private fun closeToast(){
    ToastManager.get().dismiss()
}
