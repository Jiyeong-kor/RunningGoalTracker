package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jeong.runninggoaltracker.shared.navigation.MainTab
import com.jeong.runninggoaltracker.shared.navigation.isRouteInHierarchy

@Composable
fun rememberMainNavigationState(
    navController: NavHostController,
    tabItemsByTab: Map<MainTab, MainTabItem>
): MainNavigationState {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentScreen = MainScreen.fromDestination(currentDestination)
    val activeTab = remember(currentDestination) {
        MainTab.entries.firstNotNullOfOrNull { tab ->
            val tabItem = tabItemsByTab[tab] ?: return@firstNotNullOfOrNull null
            if (currentDestination.isRouteInHierarchy(tab.route)) tabItem else null
        }
    }

    return remember(currentDestination, activeTab, currentScreen) {
        MainNavigationState(
            currentDestination = currentDestination,
            currentScreen = currentScreen,
            activeTab = activeTab,
            titleResId = currentScreen?.titleResId ?: activeTab?.titleResId,
            showBackInTopBar = currentScreen?.showBackInTopBar == true
        )
    }
}

data class MainNavigationState(
    val currentDestination: NavDestination?,
    val currentScreen: MainScreen?,
    val activeTab: MainTabItem?,
    @field:StringRes val titleResId: Int?,
    val showBackInTopBar: Boolean,
)
