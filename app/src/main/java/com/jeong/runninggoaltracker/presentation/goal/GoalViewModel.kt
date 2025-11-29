package com.jeong.runninggoaltracker.presentation.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GoalUiState(
    val currentGoalKm: Double? = null
)

class GoalViewModel(
    private val repository: RunningRepository
) : ViewModel() {

    val uiState: StateFlow<GoalUiState> =
        repository.getGoal()
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
            repository.upsertGoal(
                RunningGoal(weeklyGoalKm = km)
            )
        }
    }
}
