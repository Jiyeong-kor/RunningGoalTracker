package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.jeong.runninggoaltracker.shared.navigation.MainTab
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    mainTabItemsProvider: MainTabItemsProvider,
) : ViewModel() {

    val tabItemsByTab: Map<MainTab, MainTabItem> =
        mainTabItemsProvider.tabItemsByTab()
}
