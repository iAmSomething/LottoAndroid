package com.weeklylotto.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SingleViewModelFactory<T : ViewModel>(
    private val creator: () -> T,
) : ViewModelProvider.Factory {
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = creator.invoke() as VM
}
