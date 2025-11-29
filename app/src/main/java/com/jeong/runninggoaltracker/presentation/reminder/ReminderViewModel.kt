package com.jeong.runninggoaltracker.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReminderUiState(
    val hour: Int = 20,
    val minute: Int = 0,
    val enabled: Boolean = false
)

class ReminderViewModel(
    private val repository: RunningRepository
) : ViewModel() {

    val uiState: StateFlow<ReminderUiState> =
        repository.getReminder()
            .map { reminder ->
                if (reminder != null) {
                    ReminderUiState(
                        hour = reminder.hour,
                        minute = reminder.minute,
                        enabled = reminder.enabled
                    )
                } else {
                    ReminderUiState()
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ReminderUiState()
            )

    fun updateEnabled(enabled: Boolean) {
        val current = uiState.value
        save(current.hour, current.minute, enabled)
    }

    fun updateTime(hour: Int, minute: Int) {
        val current = uiState.value
        save(hour, minute, current.enabled)
    }

    private fun save(hour: Int, minute: Int, enabled: Boolean) {
        viewModelScope.launch {
            repository.upsertReminder(
                RunningReminder(
                    hour = hour,
                    minute = minute,
                    enabled = enabled
                )
            )
        }
    }
}
