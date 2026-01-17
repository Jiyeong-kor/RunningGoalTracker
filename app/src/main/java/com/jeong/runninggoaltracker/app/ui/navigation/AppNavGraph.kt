package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.jeong.runninggoaltracker.feature.auth.presentation.OnboardingScreen
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.shared.navigation.AuthRoute
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.composable
import com.jeong.runninggoaltracker.shared.navigation.navigateTo

@Composable
fun AppNavGraph(
    navController: NavHostController,
    activityRecognitionMonitor: ActivityRecognitionMonitor,
    requestActivityRecognitionPermission: (onResult: (Boolean) -> Unit) -> Unit,
    requestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isFirebaseInitialized = remember {
        FirebaseApp.getApps(context).isNotEmpty()
    }
    val currentUser = if (isFirebaseInitialized) Firebase.auth.currentUser else null
    val hasCompletedOnboarding = currentUser?.displayName?.isNotBlank() == true
    val startDestination = if (hasCompletedOnboarding) {
        MainNavigationRoute.Main
    } else {
        AuthRoute.Onboarding
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<AuthRoute.Onboarding> {
            OnboardingScreen(
                onComplete = {
                    navController.navigateTo(MainNavigationRoute.Main) {
                        popUpTo(AuthRoute.Onboarding) { inclusive = true }
                    }
                }
            )
        }
        mainNavGraph(
            activityRecognitionMonitor = activityRecognitionMonitor,
            requestActivityRecognitionPermission = requestActivityRecognitionPermission,
            requestTrackingPermissions = requestTrackingPermissions
        )
    }
}
