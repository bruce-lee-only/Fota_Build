package com.carota.lib.executor.ui.dialog.care

import com.carota.lib.common.uitls.LiveDataUtil
import com.carota.lib.executor.ui.BaseCare

class DialogAttentionCare : BaseCare(){
    private lateinit var liveData: LiveDataUtil<DialogAttentionCare>

    enum class DisplayOption {
        //todo: default typ, Toast will show noting and throw Exception
        DEFAULT,
        //todo: upgrade roll back, then shutdown power fail
        ROLLBACK_POWER_OFF_FAIL,
        //todo: upgrade roll back, then exit ota mode fail
        ROLLBACK_EXIT_OTA_FAIL,
        //todo: upgrade success, then shutdown power fail
        UPGRADED_POWER_OFF_FAIL,
        //todo: upgrade success, then exit ota mode fail
        UPGRADED_EXIT_OTA_FAIL,
    }

    var displayOption: DisplayOption = DisplayOption.DEFAULT

    @Suppress("UNCHECKED_CAST")
    override fun observe() {
        observer?.let {
            liveData = observer as LiveDataUtil<DialogAttentionCare>
            liveData.postValue(this)
        }
    }

    override fun unObserve() { observer = null }
}