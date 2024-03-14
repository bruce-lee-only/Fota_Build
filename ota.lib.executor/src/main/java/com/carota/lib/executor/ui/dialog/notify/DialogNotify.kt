package com.carota.lib.executor.ui.dialog.notify

import android.content.Context
import com.carota.lib.executor.databinding.DialogNotifyBinding
import com.carota.lib.executor.ui.dialog.base.BaseDialog
import com.carota.lib.executor.ui.dialog.care.DialogNotifyCare

class DialogNotify(context: Context):
    BaseDialog<DialogNotifyBinding, DialogNotifyViewModel, DialogNotifyCare>(
        DialogNotifyBinding::inflate,
        DialogNotifyViewModel::class.java,
        context
    ) {
        init { initData() }

        override fun setVariable() {
            binding.let {
                it.dialogNotifyView = this
                it.dialogNotifyViewModel = viewModel
                it.dialogNotifyCare = DialogNotifyCare()
            }
        }

        override fun setLayoutParams() {
            super.setLayoutParams(null)
        }

        override fun setDismissListener() {
            super.setDismissListener(null)
        }

        override fun setShowListener() {
            super.setShowListener {
                viewModel.countDownTime{ doOnDismiss() }
            }
        }

        override fun doOnBind(block:(String) ->Unit): DialogNotifyCare? {
            val care = viewModel.bind(block)
            return if (care != null){ care as DialogNotifyCare
            } else null
        }

        fun onConfirmClicked(){
            viewModel.confirmClicked()
            doOnDismiss()
        }
}