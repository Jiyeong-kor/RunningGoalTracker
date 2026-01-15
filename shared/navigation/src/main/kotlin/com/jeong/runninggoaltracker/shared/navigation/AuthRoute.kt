package com.jeong.runninggoaltracker.shared.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed interface AuthRoute : NavigationRoute {

    @Serializable
    data object Onboarding : AuthRoute
}
