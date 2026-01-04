package com.jeong.runninggoaltracker.app.ui.navigation

import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.MainTab

private val tabRoutes: Set<MainNavigationRoute> =
    MainTab.entries.map { it.route }.toSet()

internal fun MainNavigationRoute.isBottomTab(): Boolean =
    this in tabRoutes