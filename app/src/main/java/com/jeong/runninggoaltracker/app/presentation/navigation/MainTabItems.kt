package com.jeong.runninggoaltracker.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import com.jeong.runninggoaltracker.shared.navigation.BottomTabIcon
import com.jeong.runninggoaltracker.shared.navigation.MainTab

object MainTabItems {
    val tabItems: List<MainTabItem> = MainTab.entries.mapNotNull { tab ->
        val screen = MainScreen.fromRoute(tab.route) ?: return@mapNotNull null
        val icon = tab.icon.asImageVector() ?: return@mapNotNull null

        MainTabItem(
            tab = tab,
            titleResId = screen.titleResId,
            icon = icon
        )
    }

    val tabItemsByTab: Map<MainTab, MainTabItem> = tabItems.associateBy { it.tab }
}

private fun BottomTabIcon.asImageVector(): ImageVector? = when (this) {
    BottomTabIcon.HOME -> Icons.Filled.Home
    BottomTabIcon.RECORD -> Icons.AutoMirrored.Filled.DirectionsRun
    BottomTabIcon.REMINDER -> Icons.Filled.Notifications
}
