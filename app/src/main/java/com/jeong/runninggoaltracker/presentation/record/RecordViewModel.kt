package com.jeong.runninggoaltracker.presentation.record

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class RecordViewModel(
    private val repository: RunningRepository
) : ViewModel() {

    val records: StateFlow<List<RunningRecord>> =
        repository.getAllRecords()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    @RequiresApi(Build.VERSION_CODES.O)
    fun addRecord(distanceKm: Double, durationMinutes: Int) {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            repository.addRecord(
                RunningRecord(
                    date = today,
                    distanceKm = distanceKm,
                    durationMinutes = durationMinutes
                )
            )
        }
    }
}
