package com.carota.lib.ui.toast

import android.content.Context
import com.carota.lib.ui.BR
import com.carota.lib.ui.databinding.DialogDisclaimerBinding
import com.carota.lib.ui.toast.base.BaseDialog
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DialogDisclaimer (private val context: Context)
    : BaseDialog<DialogDisclaimerBinding, DialogDisclaimerViewModel<DialogDisclaimerBinding>>(
    DialogDisclaimerBinding::inflate,
    DialogDisclaimerViewModel::class.java,
    context
) {
        init {
            initData()
        }

    override fun setVariable() {
        binding.setVariable(BR.dialog_disclaimer_view, this)
        binding.setVariable(BR.dialog_disclaimer_view_model, viewModel)
    }
}