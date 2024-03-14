package com.carota.lib.executor.ui.uiHelper

import com.carota.lib.executor.ui.activity.mainActivity.MainActivityViewModel

class SingleClickImpl(private val viewModel: MainActivityViewModel) : SingleInterface {
    override fun onUpdateClick() {
        viewModel.onUpgradeClicked()
    }

    override fun onScheduleClick() {
        viewModel.onScheduleClicked()
    }
}