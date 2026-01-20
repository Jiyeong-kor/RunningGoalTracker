package com.jeong.runninggoaltracker.feature.reminder.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.usecase.CreateDefaultReminderUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRemindersUseCase
import com.jeong.runninggoaltracker.domain.usecase.ToggleReminderDayUseCase
import com.jeong.runninggoaltracker.feature.reminder.alarm.ReminderSchedulingInteractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderUiState(
    val id: Int,
    val hour: Int = 20,
    val minute: Int = 0,
    val enabled: Boolean = false,
    val days: Set<Int> = emptySet()
)

data class ReminderListUiState(
    val reminders: List<ReminderUiState> = emptyList()
)

@HiltViewModel
class ReminderViewModel @Inject constructor(
    getRunningRemindersUseCase: GetRunningRemindersUseCase,
    private val createDefaultReminderUseCase: CreateDefaultReminderUseCase,
    private val toggleReminderDayUseCase: ToggleReminderDayUseCase,
    private val reminderSchedulingInteractor: ReminderSchedulingInteractor
) : ViewModel() {

    val uiState: StateFlow<ReminderListUiState> =
        getRunningRemindersUseCase()
            .map { reminders ->
                ReminderListUiState(reminders.mapNotNull { it.toUiStateOrNull() })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ReminderListUiState()
            )

    fun addReminder() {
        viewModelScope.launch {
            val newReminder = createDefaultReminderUseCase()
            reminderSchedulingInteractor.saveReminder(
                updatedReminder = newReminder,
                previousReminder = null
            )
        }
    }

    fun deleteReminder(id: Int) {
        val currentReminder = uiState.value.reminders.find { it.id == id }?.toDomain() ?: return

        viewModelScope.launch {
            reminderSchedulingInteractor.deleteReminder(currentReminder)
        }
    }

    fun updateEnabled(id: Int, enabled: Boolean) =
        updateReminder(id) { reminder -> reminder.copy(enabled = enabled) }

    fun updateTime(id: Int, hour: Int, minute: Int) =
        updateReminder(id) { it.copy(hour = hour, minute = minute) }

    fun toggleDay(id: Int, day: Int) =
        updateReminder(id) { current -> toggleReminderDayUseCase(current, day) }

    private fun updateReminder(
        id: Int,
        update: (RunningReminder) -> RunningReminder
    ) {
        val currentReminderUiState = uiState.value.reminders.find { it.id == id } ?: return
        val currentRunningReminder = currentReminderUiState.toDomain()

        val updatedReminder = update(currentRunningReminder).validateEnabledDays()
        if (updatedReminder == currentRunningReminder) return

        viewModelScope.launch {
            reminderSchedulingInteractor.saveReminder(
                updatedReminder = updatedReminder,
                previousReminder = currentRunningReminder
            )
        }
    }

    private fun RunningReminder.validateEnabledDays(): RunningReminder =
        if (enabled && days.isEmpty()) {
            copy(enabled = false)
        } else {
            this
        }
}

private fun RunningReminder.toUiStateOrNull(): ReminderUiState? {
    val reminderId = id ?: return null
    return ReminderUiState(
        id = reminderId,
        hour = hour,
        minute = minute,
        enabled = enabled,
        days = days
    )
}

private fun ReminderUiState.toDomain(): RunningReminder =
    RunningReminder(
        id = id,
        hour = hour,
        minute = minute,
        enabled = enabled,
        days = days
    )
