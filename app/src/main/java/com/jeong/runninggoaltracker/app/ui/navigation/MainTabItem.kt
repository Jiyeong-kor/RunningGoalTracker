package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.jeong.runninggoaltracker.shared.navigation.MainTab

data class MainTabItem(
    val tab: MainTab,
    @field:StringRes val titleResId: Int,
    val icon: ImageVector,
)
