package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jeong.runninggoaltracker.shared.navigation.MainTab
import com.jeong.runninggoaltracker.shared.navigation.isBottomTab
import com.jeong.runninggoaltracker.shared.navigation.isRouteInHierarchy
import com.jeong.runninggoaltracker.shared.navigation.navigateTo
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.R

@Composable
fun BottomAndTopBar(
    tabItemsByTab: Map<MainTab, MainTabItem>,
    navController: NavHostController,
) {
    val density = LocalDensity.current
    val insetPx = with(density) {
        dimensionResource(R.dimen.navigation_bar_inset_none).roundToPx()
    }

    NavigationBar(windowInsets = WindowInsets(insetPx, insetPx, insetPx, insetPx)) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        MainTab.entries.forEach { tab ->
            val tabItem = tabItemsByTab[tab] ?: return@forEach
            val selected = currentDestination.isRouteInHierarchy(tabItem.tab.route)

            val onTabClick = rememberThrottleClick {
                val route = tabItem.tab.route
                if (!route.isBottomTab()) return@rememberThrottleClick

                navController.navigateTo(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            NavigationBarItem(
                selected = selected,
                onClick = onTabClick,
                icon = {
                    Icon(
                        imageVector = tabItem.icon,
                        contentDescription = stringResource(tabItem.titleResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(tabItem.titleResId),
                        style = typography.labelSmall
                    )
                }
            )
        }
    }
}
