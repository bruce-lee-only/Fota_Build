package com.carota.lib.ui.toast.attention

import android.app.Application
import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import com.carota.lib.ui.databinding.DialogAttentionBinding
import com.carota.lib.ui.toast.care.DialogAttentionCare
import com.carota.lib.ui.toast.base.BaseDialogViewModel

class DialogAttentionViewModel(application: Application):
    BaseDialogViewModel<DialogAttentionBinding, DialogAttentionCare>(application){

    override fun observe(block: (String) -> Unit): DialogAttentionCare? {
        return liveData.let {
            dialogCare?.apply {
                this.injectObserver(it)
                this.eventCallBack = block
            }
            observeHand(it)
            dialogCare
        }
    }

    override fun focusDialogCare() {
        this.dialogCare = binding.dialogAttentionCare
    }

    override fun observeHand(liveData: LiveDataUtil<DialogAttentionCare>) {
        liveData.observeForever(){ care ->
            Logger.drop("care data arrived")
            Logger.drop("observe displayOption: ${care.displayOption}")
            binding.dialogAttentionCare = care
        }
    }

    fun confirmClicked(){
        dialogCare?.run {
            this.uiEvent = eventConfirmButtonClicked
            this.eventCallBack?.invoke(eventConfirmButtonClicked)
        }
    }
}