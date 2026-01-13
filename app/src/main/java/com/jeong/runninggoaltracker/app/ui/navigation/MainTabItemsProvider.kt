package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import com.jeong.runninggoaltracker.shared.navigation.BottomTabIcon
import com.jeong.runninggoaltracker.shared.navigation.MainTab
import javax.inject.Inject

interface MainTabItemsProvider {
    fun tabItemsByTab(): Map<MainTab, MainTabItem>
}

class DefaultMainTabItemsProvider @Inject constructor() : MainTabItemsProvider {
    override fun tabItemsByTab(): Map<MainTab, MainTabItem> =
        MainTab.entries.mapNotNull { tab ->
            val screen = MainScreen.fromRoute(tab.route) ?: return@mapNotNull null
            val icon = tab.icon.asImageVector() ?: return@mapNotNull null

            MainTabItem(
                tab = tab,
                titleResId = screen.titleResId,
                icon = icon
            )
        }.associateBy { it.tab }
}

fun BottomTabIcon.asImageVector(): ImageVector? = when (this) {
    BottomTabIcon.HOME -> Icons.Filled.Home
    BottomTabIcon.RECORD -> Icons.AutoMirrored.Filled.DirectionsRun
    BottomTabIcon.REMINDER -> Icons.Filled.Notifications
    BottomTabIcon.MYPAGE -> Icons.Filled.AccountCircle
}
