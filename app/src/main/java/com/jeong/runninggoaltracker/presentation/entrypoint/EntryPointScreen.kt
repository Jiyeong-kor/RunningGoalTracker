package com.jeong.runninggoaltracker.presentation.entrypoint

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.presentation.navigation.AppNavGraph

@Composable
fun EntryPointScreen(
    activityRecognitionMonitor: ActivityRecognitionMonitor,
    requestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    AppNavGraph(
        navController = navController,
        modifier = modifier,
        activityRecognitionMonitor = activityRecognitionMonitor,
        requestTrackingPermissions = requestTrackingPermissions
    )
}
