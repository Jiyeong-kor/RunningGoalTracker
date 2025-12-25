package com.jeong.runninggoaltracker.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.jeong.runninggoaltracker.feature.home.presentation.ActivityLogUiModel
import com.jeong.runninggoaltracker.feature.goal.presentation.GoalRoute
import com.jeong.runninggoaltracker.feature.home.presentation.ActivityRecognitionUiState
import com.jeong.runninggoaltracker.feature.home.presentation.HomeRoute
import com.jeong.runninggoaltracker.feature.record.presentation.RecordRoute
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityState
import com.jeong.runninggoaltracker.feature.reminder.presentation.ReminderRoute
import kotlinx.coroutines.flow.map

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    activityRecognitionMonitor: ActivityRecognitionMonitor,
    requestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = modifier
    ) {
        composable("home") {
            HomeRoute(
                activityStateFlow = activityRecognitionMonitor.activityState.map { state ->
                    state.toUiState()
                },
                activityLogsFlow = activityRecognitionMonitor.activityLogs.map { entries ->
                    entries.map { it.toUiModel() }
                },
                onRecordClick = { navController.navigate("record") },
                onGoalClick = { navController.navigate("goal") },
                onReminderClick = { navController.navigate("reminder") }
            )
        }

        composable("record") {
            RecordRoute(
                onRequestTrackingPermissions = requestTrackingPermissions
            )
        }

        composable("goal") {
            GoalRoute(onBack = { navController.popBackStack() })
        }

        composable("reminder") { ReminderRoute() }
    }
}

private fun ActivityState.toUiState(): ActivityRecognitionUiState =
    ActivityRecognitionUiState(label = label)

private fun ActivityLogEntry.toUiModel(): ActivityLogUiModel =
    ActivityLogUiModel(
        time = time,
        label = label
    )
