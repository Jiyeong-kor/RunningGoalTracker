package com.jeong.runninggoaltracker.presentation.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jeong.runninggoaltracker.domain.repository.RunningRepository

class RecordViewModelFactory(
    private val repository: RunningRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            return RecordViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
