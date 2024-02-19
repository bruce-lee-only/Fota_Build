package com.carota.lib.ui.toast.base

interface IDialog {
    fun doOnDismiss()

    fun doOnShow()

    fun doOnBind(block: (String) -> Unit): BaseCare?
}