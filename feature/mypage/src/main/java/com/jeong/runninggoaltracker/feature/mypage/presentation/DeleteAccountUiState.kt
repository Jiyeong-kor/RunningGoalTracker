package com.jeong.runninggoaltracker.feature.mypage.presentation

import com.jeong.runninggoaltracker.domain.model.AuthError

sealed interface DeleteAccountUiState {
    data object Idle : DeleteAccountUiState
    data object Loading : DeleteAccountUiState
    data object Success : DeleteAccountUiState
    data class Failure(val error: AuthError) : DeleteAccountUiState
}
