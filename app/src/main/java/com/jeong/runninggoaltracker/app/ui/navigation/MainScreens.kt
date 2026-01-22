package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.annotation.StringRes
import androidx.navigation.NavDestination
import com.jeong.runninggoaltracker.R
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.isRouteInHierarchy

sealed interface MainScreen {
    val route: MainNavigationRoute

    @get:StringRes
    val titleResId: Int
    val showBackInTopBar: Boolean

    data object Home : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.Home
        override val titleResId: Int = R.string.title_home
        override val showBackInTopBar: Boolean = false
    }

    data object Record : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.Record
        override val titleResId: Int = R.string.title_record
        override val showBackInTopBar: Boolean = true
    }

    data object AiCoach : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.AiCoach
        override val titleResId: Int = R.string.title_ai_coach
        override val showBackInTopBar: Boolean = false
    }

    data object Goal : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.Goal
        override val titleResId: Int = R.string.title_goal
        override val showBackInTopBar: Boolean = true
    }

    data object Reminder : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.Reminder
        override val titleResId: Int = R.string.title_reminder
        override val showBackInTopBar: Boolean = true
    }

    data object MyPage : MainScreen {
        override val route: MainNavigationRoute = MainNavigationRoute.MyPage
        override val titleResId: Int = R.string.title_mypage
        override val showBackInTopBar: Boolean = false
    }

    companion object {
        private val screenDescriptors: List<MainScreen> =
            listOf(Home, Record, AiCoach, Goal, Reminder, MyPage)
        private val screenByRoute: Map<MainNavigationRoute, MainScreen> =
            screenDescriptors.associateBy { it.route }

        val entries: List<MainScreen> = screenDescriptors

        fun fromRoute(route: MainNavigationRoute): MainScreen? = screenByRoute[route]

        fun fromDestination(destination: NavDestination?): MainScreen? =
            entries.firstOrNull { screen -> destination.isRouteInHierarchy(screen.route) }
    }
}
