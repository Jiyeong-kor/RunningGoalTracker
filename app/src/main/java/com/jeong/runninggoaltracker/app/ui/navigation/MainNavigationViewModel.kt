package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.jeong.runninggoaltracker.shared.navigation.MainTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainNavigationViewModel @Inject constructor(
    mainTabItemsProvider: MainTabItemsProvider,
) : ViewModel() {

    private val _tabItemsByTab =
        MutableStateFlow<Map<MainTab, MainTabItem>>(mainTabItemsProvider.tabItemsByTab())
    val tabItemsByTab: StateFlow<Map<MainTab, MainTabItem>> = _tabItemsByTab.asStateFlow()
}
