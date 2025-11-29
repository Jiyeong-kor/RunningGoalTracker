package com.jeong.runninggoaltracker.presentation.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeong.runninggoaltracker.domain.repository.RunningRepository

class ReminderViewModelFactory(
    private val repository: RunningRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            return ReminderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
