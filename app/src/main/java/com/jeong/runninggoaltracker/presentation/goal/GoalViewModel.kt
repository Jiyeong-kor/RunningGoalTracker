package com.jeong.runninggoaltracker.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.GetRunningGoalUseCase
import com.jeong.runninggoaltracker.domain.usecase.UpsertRunningGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalUiState(
    val currentGoalKm: Double? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    getRunningGoalUseCase: GetRunningGoalUseCase,
    private val upsertRunningGoalUseCase: UpsertRunningGoalUseCase
) : ViewModel() {

    val uiState: StateFlow<GoalUiState> =
        getRunningGoalUseCase()
            .map { goal ->
                GoalUiState(currentGoalKm = goal?.weeklyGoalKm)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = GoalUiState()
            )

    fun saveGoal(km: Double) {
        viewModelScope.launch {
            upsertRunningGoalUseCase(km)
        }
    }
}
