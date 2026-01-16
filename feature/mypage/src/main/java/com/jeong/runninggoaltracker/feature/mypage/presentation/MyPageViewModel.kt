package com.jeong.runninggoaltracker.feature.mypage.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.GetRunningGoalUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningSummaryUseCase
import com.jeong.runninggoaltracker.domain.usecase.ObserveIsAnonymousUseCase
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
    private val observeIsAnonymousUseCase: ObserveIsAnonymousUseCase
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
                getRunningGoalUseCase(),
                observeIsAnonymousUseCase()
            ) { summary, goal, isAnonymous ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        summary = summary,
                        goal = goal,
                        isAnonymous = isAnonymous
                    )
                }
            }.collect {}
        }
    }

    fun toggleActivityRecognition(enabled: Boolean) {
        _uiState.update { it.copy(isActivityRecognitionEnabled = enabled) }
    }
}
