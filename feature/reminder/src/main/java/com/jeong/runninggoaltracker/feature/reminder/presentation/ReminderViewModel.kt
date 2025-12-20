package com.jeong.runninggoaltracker.feature.reminder.presentation

import android.os.Build
import androidx.annotation.RequiresApi
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
import java.time.DayOfWeek
import javax.inject.Inject

data class ReminderUiState(
    val id: Int? = null,
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

    @RequiresApi(Build.VERSION_CODES.O)
    val uiState: StateFlow<ReminderListUiState> =
        getRunningRemindersUseCase()
            .map { reminders -> ReminderListUiState(reminders.map { it.toUiState() }) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ReminderListUiState()
            )

    @RequiresApi(Build.VERSION_CODES.O)
    fun addReminder() {
        viewModelScope.launch {
            val newReminder = createDefaultReminderUseCase()
            reminderSchedulingInteractor.saveReminder(
                updatedReminder = newReminder,
                previousReminder = null
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteReminder(id: Int) {
        val currentReminder = uiState.value.reminders.find { it.id == id }?.toDomainOrNull()
            ?: return

        viewModelScope.launch {
            reminderSchedulingInteractor.deleteReminder(currentReminder)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateEnabled(id: Int, enabled: Boolean) {
        updateReminder(id) { reminder -> reminder.copy(enabled = enabled) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateTime(id: Int, hour: Int, minute: Int) {
        updateReminder(id) { it.copy(hour = hour, minute = minute) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleDay(id: Int, day: Int) {
        updateReminder(id) { current -> toggleReminderDayUseCase(current, day) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateReminder(
        id: Int,
        update: (RunningReminder) -> RunningReminder
    ) {
        val currentReminderUiState = uiState.value.reminders.find { it.id == id } ?: return
        val currentRunningReminder = currentReminderUiState.toDomainOrNull() ?: return

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

@RequiresApi(Build.VERSION_CODES.O)
private fun RunningReminder.toUiState(): ReminderUiState =
    ReminderUiState(
        id = id,
        hour = hour,
        minute = minute,
        enabled = enabled,
        days = days.map { it.value }.toSet()
    )

@RequiresApi(Build.VERSION_CODES.O)
private fun ReminderUiState.toDomain(): RunningReminder =
    RunningReminder(
        id = id,
        hour = hour,
        minute = minute,
        enabled = enabled,
        days = days.map { DayOfWeek.of(it) }.toSet()
    )

@RequiresApi(Build.VERSION_CODES.O)
private fun ReminderUiState.toDomainOrNull(): RunningReminder? =
    runCatching { toDomain() }.getOrNull()
