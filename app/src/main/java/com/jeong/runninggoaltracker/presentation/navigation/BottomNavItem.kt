package com.jeong.runninggoaltracker.presentation.navigation

data class BottomNavItem(
    val route: String,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = "home",
        label = "홈"
    ),
    BottomNavItem(
        route = "record",
        label = "기록"
    ),
    BottomNavItem(
        route = "reminder",
        label = "알림"
    )
)
