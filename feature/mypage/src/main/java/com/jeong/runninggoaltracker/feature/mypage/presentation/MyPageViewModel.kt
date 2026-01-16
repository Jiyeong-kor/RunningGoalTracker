package com.jeong.runninggoaltracker.feature.mypage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.DeleteAccountUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningGoalUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningSummaryUseCase
import com.jeong.runninggoaltracker.domain.usecase.ObserveIsAnonymousUseCase
import com.jeong.runninggoaltracker.domain.usecase.ObserveUserNicknameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel @Inject constructor(
    private val getRunningSummaryUseCase: GetRunningSummaryUseCase,
    private val getRunningGoalUseCase: GetRunningGoalUseCase,
    private val observeIsAnonymousUseCase: ObserveIsAnonymousUseCase,
    private val observeUserNicknameUseCase: ObserveUserNicknameUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    private val _deleteAccountState =
        MutableStateFlow<DeleteAccountUiState>(DeleteAccountUiState.Idle)
    val deleteAccountState: StateFlow<DeleteAccountUiState> = _deleteAccountState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                getRunningSummaryUseCase(),
                getRunningGoalUseCase(),
                observeIsAnonymousUseCase(),
                observeUserNicknameUseCase()
            ) { summary, goal, isAnonymous, nickname ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = summary,
                        goal = goal,
                        isAnonymous = isAnonymous,
                        userNickname = nickname
                    )
                }
            }.collect {}
        }
    }

    fun toggleActivityRecognition(enabled: Boolean) {
        _uiState.update { it.copy(isActivityRecognitionEnabled = enabled) }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            if (_deleteAccountState.value is DeleteAccountUiState.Loading) {
                return@launch
            }
            _deleteAccountState.value = DeleteAccountUiState.Loading
            when (val result = deleteAccountUseCase()) {
                is com.jeong.runninggoaltracker.domain.model.AuthResult.Success -> {
                    _deleteAccountState.value = DeleteAccountUiState.Success
                }

                is com.jeong.runninggoaltracker.domain.model.AuthResult.Failure -> {
                    _deleteAccountState.value = DeleteAccountUiState.Failure(result.error)
                }
            }
        }
    }

    fun resetDeleteAccountState() {
        _deleteAccountState.value = DeleteAccountUiState.Idle
    }
}
