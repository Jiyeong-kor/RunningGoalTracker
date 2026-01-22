package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.jeong.runninggoaltracker.feature.ai_coach.presentation.SmartWorkoutRoute
import com.jeong.runninggoaltracker.feature.ai_coach.presentation.AiCoachViewModel
import com.jeong.runninggoaltracker.feature.goal.presentation.GoalRoute
import com.jeong.runninggoaltracker.feature.goal.presentation.GoalViewModel
import com.jeong.runninggoaltracker.feature.home.presentation.HomeRoute
import com.jeong.runninggoaltracker.feature.home.presentation.HomeViewModel
import com.jeong.runninggoaltracker.feature.mypage.presentation.MyPageRoute
import com.jeong.runninggoaltracker.feature.mypage.presentation.MyPageViewModel
import com.jeong.runninggoaltracker.feature.record.presentation.RecordRoute
import com.jeong.runninggoaltracker.feature.record.viewmodel.RecordViewModel
import com.jeong.runninggoaltracker.feature.reminder.presentation.ReminderRoute
import com.jeong.runninggoaltracker.feature.reminder.presentation.ReminderViewModel
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.composable
import com.jeong.runninggoaltracker.shared.navigation.navigateTo

fun NavGraphBuilder.mainNavGraph(
) = composable<MainNavigationRoute.Main> {
    MainContainerRoute(
    )
}

internal fun NavGraphBuilder.mainDestinations(
    navController: NavHostController
) {
    val onBack: () -> Unit = { navController.popBackStack() }

    composable<MainNavigationRoute.Home> { backStackEntry ->
        val viewModel: HomeViewModel = hiltViewModel(backStackEntry)
        HomeRoute(
            viewModel = viewModel,
            onNavigateToRecord = { navController.navigateTo(MainNavigationRoute.Record) },
            onNavigateToGoal = { navController.navigateTo(MainNavigationRoute.Goal) },
            onNavigateToReminder = { navController.navigateTo(MainNavigationRoute.Reminder) }
        )
    }

    composable<MainNavigationRoute.Record> { backStackEntry ->
        val viewModel: RecordViewModel = hiltViewModel(backStackEntry)
        RecordRoute(viewModel = viewModel)
    }

    composable<MainNavigationRoute.AiCoach> { backStackEntry ->
        val viewModel: AiCoachViewModel = hiltViewModel(backStackEntry)
        SmartWorkoutRoute(
            onBack = onBack,
            viewModel = viewModel
        )
    }

    composable<MainNavigationRoute.Goal> { backStackEntry ->
        val viewModel: GoalViewModel = hiltViewModel(backStackEntry)
        GoalRoute(
            viewModel = viewModel,
            onBack = onBack
        )
    }

    composable<MainNavigationRoute.Reminder> { backStackEntry ->
        val viewModel: ReminderViewModel = hiltViewModel(backStackEntry)
        ReminderRoute(viewModel = viewModel)
    }

    composable<MainNavigationRoute.MyPage> { backStackEntry ->
        val viewModel: MyPageViewModel = hiltViewModel(backStackEntry)
        MyPageRoute(
            viewModel = viewModel,
            onNavigateToGoal = { navController.navigateTo(MainNavigationRoute.Goal) },
            onNavigateToReminder = { navController.navigateTo(MainNavigationRoute.Reminder) }
        )
    }
}
