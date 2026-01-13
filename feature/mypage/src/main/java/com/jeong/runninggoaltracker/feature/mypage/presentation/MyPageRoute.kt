package com.jeong.runninggoaltracker.feature.mypage.presentation

import androidx.compose.runtime.Composable

@Composable
fun MyPageRoute(
    viewModel: MyPageViewModel,
    onNavigateToGoal: () -> Unit,
    onNavigateToReminder: () -> Unit
) {
    MyPageScreen(
        viewModel = viewModel,
        onNavigateToGoal = onNavigateToGoal,
        onNavigateToReminder = onNavigateToReminder
    )
}
