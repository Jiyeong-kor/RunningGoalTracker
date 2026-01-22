package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.jeong.runninggoaltracker.feature.auth.presentation.OnboardingScreen
import com.jeong.runninggoaltracker.shared.navigation.AuthRoute
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.composable
import com.jeong.runninggoaltracker.shared.navigation.navigateTo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: AppNavGraphViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val startDestination = uiState.startDestination

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
        )
    }
}
