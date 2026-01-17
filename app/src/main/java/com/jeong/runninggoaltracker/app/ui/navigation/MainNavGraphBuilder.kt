package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.jeong.runninggoaltracker.feature.ai_coach.presentation.SmartWorkoutRoute
import com.jeong.runninggoaltracker.feature.goal.presentation.GoalRoute
import com.jeong.runninggoaltracker.feature.goal.presentation.GoalViewModel
import com.jeong.runninggoaltracker.feature.home.presentation.HomeRoute
import com.jeong.runninggoaltracker.feature.home.presentation.HomeViewModel
import com.jeong.runninggoaltracker.feature.mypage.presentation.MyPageRoute
import com.jeong.runninggoaltracker.feature.mypage.presentation.MyPageViewModel
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.feature.record.presentation.RecordRoute
import com.jeong.runninggoaltracker.feature.record.viewmodel.RecordViewModel
import com.jeong.runninggoaltracker.feature.reminder.presentation.ReminderRoute
import com.jeong.runninggoaltracker.feature.reminder.presentation.ReminderViewModel
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.composable
import com.jeong.runninggoaltracker.shared.navigation.navigateTo
import kotlinx.coroutines.flow.map

fun NavGraphBuilder.mainNavGraph(
    activityRecognitionMonitor: ActivityRecognitionMonitor,
    requestActivityRecognitionPermission: (onResult: (Boolean) -> Unit) -> Unit,
    requestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit
) {
    composable<MainNavigationRoute.Main> {
        MainContainerRoute(
            activityRecognitionMonitor = activityRecognitionMonitor,
            requestActivityRecognitionPermission = requestActivityRecognitionPermission,
            requestTrackingPermissions = requestTrackingPermissions
        )
    }
}

internal fun NavGraphBuilder.mainDestinations(
    navController: NavHostController,
    activityRecognitionMonitor: ActivityRecognitionMonitor,
    requestActivityRecognitionPermission: (onResult: (Boolean) -> Unit) -> Unit,
    requestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit
) {
    composable<MainNavigationRoute.Home> { backStackEntry ->
        val viewModel: HomeViewModel = hiltViewModel(backStackEntry)
        HomeRoute(
            viewModel = viewModel,
            activityStateFlow = activityRecognitionMonitor.activityState.map { state ->
                state.toUiState()
            },
            activityLogsFlow = activityRecognitionMonitor.activityLogs.map { entries ->
                entries.map { it.toUiModel() }
            },
            onRecordClick = { navController.navigateTo(MainNavigationRoute.Record) },
            onGoalClick = { navController.navigateTo(MainNavigationRoute.Goal) },
            onReminderClick = { navController.navigateTo(MainNavigationRoute.Reminder) }
        )
    }

    composable<MainNavigationRoute.Record> { backStackEntry ->
        val viewModel: RecordViewModel = hiltViewModel(backStackEntry)
        RecordRoute(
            viewModel = viewModel,
            onRequestActivityRecognitionPermission = requestActivityRecognitionPermission,
            onRequestTrackingPermissions = requestTrackingPermissions
        )
    }

    composable<MainNavigationRoute.AiCoach> {
        SmartWorkoutRoute(
            onBack = { navController.popBackStack() }
        )
    }

    composable<MainNavigationRoute.Goal> { backStackEntry ->
        val viewModel: GoalViewModel = hiltViewModel(backStackEntry)
        GoalRoute(
            viewModel = viewModel,
            onBack = { navController.popBackStack() }
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
