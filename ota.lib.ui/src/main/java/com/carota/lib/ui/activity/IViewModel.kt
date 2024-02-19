package com.carota.lib.ui.activity

import android.content.Context
import androidx.databinding.ViewDataBinding

interface IViewModel<VB: ViewDataBinding> {

    fun bindData(binding: VB)
}