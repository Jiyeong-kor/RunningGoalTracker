package com.jeong.runninggoaltracker.feature.mypage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.GetRunningGoalUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningSummaryUseCase
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
    private val getRunningGoalUseCase: GetRunningGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyPageUiState())
    val uiState: StateFlow<MyPageUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                getRunningSummaryUseCase(),
                getRunningGoalUseCase()
            ) { summary, goal ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = summary,
                        goal = goal
                    )
                }
            }.collect {}
        }
    }

    fun toggleActivityRecognition(enabled: Boolean) {
        _uiState.update { it.copy(isActivityRecognitionEnabled = enabled) }
    }
}
