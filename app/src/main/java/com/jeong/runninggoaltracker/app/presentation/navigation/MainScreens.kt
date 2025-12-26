package com.jeong.runninggoaltracker.app.presentation.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import com.jeong.runninggoaltracker.R
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.isRouteInHierarchy

sealed interface MainScreen {
    val route: MainNavigationRoute
    @get:StringRes val titleResId: Int

    data object Home : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.Home
        override val titleResId: Int = R.string.title_home
    }

    data object Record : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.Record
        override val titleResId: Int = R.string.title_record
    }

    data object Goal : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.Goal
        override val titleResId: Int = R.string.title_goal
    }

    data object Reminder : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.Reminder
        override val titleResId: Int = R.string.title_reminder
    }

    companion object {
        private val screenDescriptors: List<MainScreen> = listOf(Home, Record, Goal, Reminder)
        private val screenByRoute: Map<MainNavigationRoute, MainScreen> =
            screenDescriptors.associateBy { it.route }

        val entries: List<MainScreen> = screenDescriptors

        fun fromRoute(route: MainNavigationRoute): MainScreen? = screenByRoute[route]

        fun fromDestination(destination: NavDestination?): MainScreen? =
            entries.firstOrNull { screen -> destination.isRouteInHierarchy(screen.route) }
    }
}
