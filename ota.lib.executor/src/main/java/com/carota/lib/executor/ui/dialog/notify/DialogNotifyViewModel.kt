package com.carota.lib.executor.ui.dialog.notify

import android.app.Application
import com.carota.lib.common.uitls.CountdownUtil
import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.common.uitls.Logger
import com.carota.lib.executor.databinding.DialogNotifyBinding
import com.carota.lib.executor.ui.dialog.base.BaseDialogViewModel
import com.carota.lib.executor.ui.dialog.base.basedialog.dismissToast
import com.carota.lib.executor.ui.dialog.care.DialogNotifyCare
import kotlinx.coroutines.Job

class DialogNotifyViewModel(application: Application):
    BaseDialogViewModel<DialogNotifyBinding, DialogNotifyCare>(application){

    private var job: Job = Job()

    override fun observe(block: (String) -> Unit): DialogNotifyCare? {
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
        this.dialogCare = binding.dialogNotifyCare
    }

    override fun observeHand(liveData: LiveDataUtil<DialogNotifyCare>) {
        liveData.observeForever{ care ->
            Logger.drop("care data arrived")
            binding.dialogNotifyCare = care
        }
    }


    fun confirmClicked(){
        job.cancel()
        dialogCare?.run {
            this.uiEvent = eventConfirmButtonClicked
            this.eventCallBack?.invoke(eventConfirmButtonClicked)
        }
    }

    fun countDownTime(end:()->Unit){
        job = CountdownUtil.countdown(5){ end() }
    }
}