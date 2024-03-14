package com.carota.lib.executor.ui.dialog.attention

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.carota.lib.common.uitls.Logger
import com.carota.lib.executor.R
import com.carota.lib.executor.ui.dialog.care.DialogAttentionCare

internal class DialogAttentionAdapter {
    companion object{
        @BindingAdapter(value = ["dialog_attention_care"], requireAll = true)
        fun syncDialogCare(textView: TextView, care: DialogAttentionCare){
            when(care.displayOption){
                DialogAttentionCare.DisplayOption.ROLLBACK_POWER_OFF_FAIL   -> {
                    textView.setText(R.string.dialog_attention_rollback_power_off_fail)
                }
                DialogAttentionCare.DisplayOption.ROLLBACK_EXIT_OTA_FAIL   -> {
                    textView.setText(R.string.dialog_attention_rollback_exit_ota_fail)
                }
                DialogAttentionCare.DisplayOption.UPGRADED_POWER_OFF_FAIL   -> {
                    textView.setText(R.string.dialog_attention_upgraded_power_off_fail)
                }
                DialogAttentionCare.DisplayOption.UPGRADED_EXIT_OTA_FAIL   -> {
                    textView.setText(R.string.dialog_attention_upgraded_exit_ota_fail)
                }
                else ->{ Logger.error("${this::class.simpleName} get error display option: ${care.displayOption}")}
            }
        }
    }
}