package com.carota.lib.ui.toast.base

import android.app.Application
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.AndroidViewModel

open class BaseDialogViewModel<VB : ViewDataBinding>(application: Application) : AndroidViewModel(
    application
) {
}