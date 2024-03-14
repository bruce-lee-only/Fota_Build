package com.carota.lib.executor.ui.dialog.base

interface IDialog {
    fun doOnDismiss()

    fun doOnShow()

    fun doOnBind(block: (String) -> Unit): BaseCare?
}