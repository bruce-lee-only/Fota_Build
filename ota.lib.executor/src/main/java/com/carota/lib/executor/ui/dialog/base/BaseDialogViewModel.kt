package com.carota.lib.executor.ui.dialog.base

import android.app.Application
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.AndroidViewModel
import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.executor.ui.BaseCare

abstract class BaseDialogViewModel<VB : ViewDataBinding, T: BaseCare>(application: Application)
    : AndroidViewModel(application) {

    var liveData : LiveDataUtil<T> = LiveDataUtil()

    var dialogCare: T? = null

    lateinit var binding: VB

    open fun bind(block: (String) -> Unit): BaseCare?{
        this.focusDialogCare()
        return observe(block)
    }

    abstract fun observe(block: (String) -> Unit): BaseCare?

    abstract fun observeHand(liveData: LiveDataUtil<T>)

    abstract fun focusDialogCare()
}