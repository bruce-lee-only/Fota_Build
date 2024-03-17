package com.carota.lib.executor.ui

import com.carota.lib.common.uitls.LiveDataUtil

abstract class BaseCare {

    //todo: base ui event defined
    open val eventConfirmButtonClicked  = "event_confirm_button_click_${this::class.simpleName}"
    open val eventCancelButtonClicked   = "event_cancel_button_click_${this::class.simpleName}"
    open val eventTimeOut               = "event_time_out_${this::class.simpleName}"
    open var uiEvent: String = ""

    //todo: options help to display our ui
    open var isRebootDisplay: Boolean = false

    internal open var eventCallBack: ((String) -> Unit)? = null

    internal open var observer: LiveDataUtil<out BaseCare>? = null

    internal open fun injectObserver(observer: LiveDataUtil<out BaseCare>){
        this.observer = observer
    }

    open fun sync(){ observe() }

    internal abstract fun observe()

    internal abstract fun unObserve()

    //fixme: here should make sure if we received a expect event
}

