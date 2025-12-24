package com.jeong.runninggoaltracker.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.GetRunningSummaryUseCase
import com.jeong.runninggoaltracker.domain.util.DateFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class HomeUiState(
    val weeklyGoalKm: Double? = null,
    val totalThisWeekKm: Double = 0.0,
    val recordCountThisWeek: Int = 0,
    val progress: Float = 0f
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getRunningSummaryUseCase: GetRunningSummaryUseCase,
    val dateFormatter: DateFormatter
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        getRunningSummaryUseCase()
            .map { summary ->
                HomeUiState(
                    weeklyGoalKm = summary.weeklyGoalKm,
                    totalThisWeekKm = summary.totalThisWeekKm,
                    recordCountThisWeek = summary.recordCountThisWeek,
                    progress = summary.progress
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = HomeUiState()
            )
}
