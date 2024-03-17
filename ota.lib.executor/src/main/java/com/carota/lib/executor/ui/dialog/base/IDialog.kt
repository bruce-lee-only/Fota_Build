package com.carota.lib.executor.ui.dialog.base

import com.carota.lib.executor.ui.BaseCare

interface IDialog {
    fun doOnDismiss()

    fun doOnShow()

    fun doOnBind(block: (String) -> Unit): BaseCare?
}