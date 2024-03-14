package com.carota.lib.executor.ui.dialog.care

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.executor.ui.dialog.base.BaseCare

class DialogNotifyCare : BaseCare(){
    private lateinit var liveData: LiveDataUtil<DialogNotifyCare>

    @Suppress("UNCHECKED_CAST")
    override fun observe() {
        observer?.let {
            liveData = observer as LiveDataUtil<DialogNotifyCare>
            liveData.postValue(this)
        }
    }

    override fun unObserve() { observer = null }
}