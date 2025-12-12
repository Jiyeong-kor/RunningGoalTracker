package com.jeong.runninggoaltracker.feature.goal.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.usecase.GetRunningGoalUseCase
import com.jeong.runninggoaltracker.domain.usecase.UpsertRunningGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GoalUiState(
    val currentGoalKm: Double? = null,
    val weeklyGoalInput: String = "",
    val error: GoalInputError? = null
)

enum class GoalInputError {
    INVALID_NUMBER,
    NON_POSITIVE
}

private data class GoalInputState(
    val weeklyGoalInput: String = "",
    val error: GoalInputError? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    getRunningGoalUseCase: GetRunningGoalUseCase,
    private val upsertRunningGoalUseCase: UpsertRunningGoalUseCase
) : ViewModel() {

    private val inputState = MutableStateFlow(GoalInputState())

    val uiState: StateFlow<GoalUiState> = combine(
        getRunningGoalUseCase(),
        inputState
    ) { goal, input ->
        GoalUiState(
            currentGoalKm = goal?.weeklyGoalKm,
            weeklyGoalInput = input.weeklyGoalInput.ifEmpty {
                goal?.weeklyGoalKm?.toString().orEmpty()
            },
            error = input.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GoalUiState()
    )

    fun onWeeklyGoalChanged(value: String) {
        inputState.update { current ->
            current.copy(
                weeklyGoalInput = value,
                error = null
            )
        }
    }

    fun saveGoal(onSuccess: () -> Unit) {
        val weeklyGoal = inputState.value.weeklyGoalInput.toDoubleOrNull()
        when {
            weeklyGoal == null -> {
                inputState.update { current ->
                    current.copy(error = GoalInputError.INVALID_NUMBER)
                }
            }

            weeklyGoal <= 0.0 -> {
                inputState.update { current ->
                    current.copy(error = GoalInputError.NON_POSITIVE)
                }
            }

            else -> {
                viewModelScope.launch {
                    upsertRunningGoalUseCase(RunningGoal(weeklyGoalKm = weeklyGoal))
                    inputState.update { current ->
                        current.copy(error = null)
                    }
                    onSuccess()
                }
            }
        }
    }
}
