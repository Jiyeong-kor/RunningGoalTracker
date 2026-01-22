package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.R
import com.jeong.runninggoaltracker.shared.designsystem.common.AppTopBar
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.MainTab

@Composable
fun MainContainerRoute(
) {
    val viewModel = hiltViewModel<MainNavigationViewModel>()
    val navController = rememberNavController()
    val tabItemsByTab by viewModel.tabItemsByTab.collectAsState()
    val navigationState = rememberMainNavigationState(
        navController = navController,
        tabItemsByTab = tabItemsByTab
    )
    val startDestination = remember {
        MainTab.entries.firstOrNull()?.route ?: MainNavigationRoute.Home
    }

    Scaffold(
        topBar = {
            AppTopBar(
                titleResId = navigationState.titleResId,
                fallbackTitleResId = R.string.app_name_full,
                onBack = if (navigationState.showBackInTopBar) {
                    { navController.popBackStack() }
                } else {
                    null
                }
            )
        },
        bottomBar = {
            BottomAndTopBar(
                tabItemsByTab = tabItemsByTab,
                navController = navController
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            mainDestinations(
                navController = navController
            )
        }
    }
}
