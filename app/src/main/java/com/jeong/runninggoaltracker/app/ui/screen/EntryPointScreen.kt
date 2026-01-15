package com.jeong.runninggoaltracker.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jeong.runninggoaltracker.app.ui.navigation.AppNavGraph
import com.jeong.runninggoaltracker.app.ui.permission.CameraPermissionHandler
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor

@Composable
fun EntryPointScreen(
    activityRecognitionMonitor: ActivityRecognitionMonitor,
    requestActivityRecognitionPermission: (onResult: (Boolean) -> Unit) -> Unit,
    requestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit,
    requestCameraPermission: (onResult: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    CameraPermissionHandler(
        requestCameraPermission = requestCameraPermission,
        requestKey = Unit
    )

    AppNavGraph(
        navController = navController,
        modifier = modifier,
        activityRecognitionMonitor = activityRecognitionMonitor,
        requestActivityRecognitionPermission = requestActivityRecognitionPermission,
        requestTrackingPermissions = requestTrackingPermissions,
        requestCameraPermission = requestCameraPermission
    )
}
