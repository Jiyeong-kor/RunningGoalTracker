package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    activityRecognitionMonitor: ActivityRecognitionMonitor,
    requestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = MainNavigationRoute.Main,
        modifier = modifier
    ) {
        mainNavGraph(
            activityRecognitionMonitor = activityRecognitionMonitor,
            requestTrackingPermissions = requestTrackingPermissions
        )
    }
}
