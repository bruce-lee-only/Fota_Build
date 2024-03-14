package com.carota.lib.executor.ui.dialog.attention

import android.content.Context
import com.carota.lib.executor.databinding.DialogAttentionBinding
import com.carota.lib.executor.ui.dialog.base.BaseDialog
import com.carota.lib.executor.ui.dialog.care.DialogAttentionCare

class DialogAttention(context: Context):
    BaseDialog<DialogAttentionBinding, DialogAttentionViewModel, DialogAttentionCare>(
        DialogAttentionBinding::inflate,
        DialogAttentionViewModel::class.java,
        context
    ) {

    init { initData() }

    override fun setVariable() {
        binding.let {
            it.dialogAttentionView = this
            it.dialogAttentionViewModel = viewModel
            it.dialogAttentionCare = DialogAttentionCare()
        }
    }

    override fun setLayoutParams() {
        super.setLayoutParams(null)
    }

    override fun setDismissListener() {
        super.setDismissListener(null)
    }

    override fun setShowListener() {
        super.setShowListener(null)
    }

    override fun doOnBind(block:(String) ->Unit): DialogAttentionCare? {
        val care = viewModel.bind(block)
        return if (care != null){ care as DialogAttentionCare
        } else null
    }

    fun onConfirmClicked(){
        viewModel.confirmClicked()
    }
}