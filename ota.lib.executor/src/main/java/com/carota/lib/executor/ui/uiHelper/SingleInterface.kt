package com.carota.lib.executor.ui.uiHelper

interface SingleInterface {
    @Debounce(interval = 3000)
    fun onUpdateClick()

    @Debounce(interval = 3000)
    fun onScheduleClick()
}