package com.jeong.runninggoaltracker.presentation.navigation

import androidx.annotation.StringRes
import com.jeong.runninggoaltracker.R

data class BottomNavItem(
    val route: String,
    @param:StringRes val labelResId: Int
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = "home",
        labelResId = R.string.nav_home
    ),
    BottomNavItem(
        route = "record",
        labelResId = R.string.nav_record
    ),
    BottomNavItem(
        route = "reminder",
        labelResId = R.string.nav_reminder
    )
)
