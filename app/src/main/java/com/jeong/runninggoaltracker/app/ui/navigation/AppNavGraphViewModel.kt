package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.jeong.runninggoaltracker.shared.navigation.AuthRoute
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.NavigationRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppNavGraphUiState(
    val startDestination: NavigationRoute = AuthRoute.Onboarding
)

@HiltViewModel
class AppNavGraphViewModel @Inject constructor() : ViewModel() {

    private val _uiState =
        MutableStateFlow(AppNavGraphUiState(startDestination = startDestination()))
    val uiState: StateFlow<AppNavGraphUiState> = _uiState.asStateFlow()

    private fun startDestination(): NavigationRoute =
        if (Firebase.auth.currentUser?.displayName?.isNotBlank() == true) {
            MainNavigationRoute.Main
        } else {
            AuthRoute.Onboarding
        }
}
