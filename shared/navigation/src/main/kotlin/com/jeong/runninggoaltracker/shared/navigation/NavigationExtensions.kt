package com.jeong.runninggoaltracker.shared.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable

fun NavHostController.navigateTo(
    route: NavigationRoute,
    builder: NavOptionsBuilder.() -> Unit = {},
) {
    navigate(route, builder)
}

inline fun <reified T : NavigationRoute> NavGraphBuilder.composable(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable<T>(content = content)
}

inline fun <reified T : NavigationRoute> NavDestination?.isRouteInHierarchy(): Boolean =
    this?.hierarchy?.any { destination ->
        destination.route == requireNotNull(T::class.qualifiedName)
    } == true

fun NavDestination?.isRouteInHierarchy(route: MainNavigationRoute): Boolean = when (route) {
    MainNavigationRoute.Main -> isRouteInHierarchy<MainNavigationRoute.Main>()
    MainNavigationRoute.Home -> isRouteInHierarchy<MainNavigationRoute.Home>()
    MainNavigationRoute.Record -> isRouteInHierarchy<MainNavigationRoute.Record>()
    MainNavigationRoute.Goal -> isRouteInHierarchy<MainNavigationRoute.Goal>()
    MainNavigationRoute.Reminder -> isRouteInHierarchy<MainNavigationRoute.Reminder>()
    MainNavigationRoute.MyPage -> isRouteInHierarchy<MainNavigationRoute.MyPage>()
}
