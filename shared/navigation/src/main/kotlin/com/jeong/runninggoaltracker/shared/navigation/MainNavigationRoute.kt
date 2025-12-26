package com.jeong.runninggoaltracker.shared.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavigationRoute

@Serializable
sealed interface FeatureNavigationRoute : NavigationRoute

@Serializable
sealed interface MainNavigationRoute : FeatureNavigationRoute {

    @Serializable
    data object Main : MainNavigationRoute

    @Serializable
    data object Home : MainNavigationRoute

    @Serializable
    data object Record : MainNavigationRoute

    @Serializable
    data object Goal : MainNavigationRoute

    @Serializable
    data object Reminder : MainNavigationRoute
}
