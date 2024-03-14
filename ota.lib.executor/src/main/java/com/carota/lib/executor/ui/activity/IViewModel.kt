package com.carota.lib.executor.ui.activity

import androidx.databinding.ViewDataBinding

interface IViewModel<VB: ViewDataBinding> {

    fun bindData(binding: VB)
}