package com.jeong.runninggoaltracker.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.GetRunningSummaryUseCase
import com.jeong.runninggoaltracker.feature.home.contract.HOME_ZERO_DOUBLE
import com.jeong.runninggoaltracker.feature.home.contract.HOME_ZERO_FLOAT
import com.jeong.runninggoaltracker.feature.home.contract.HOME_ZERO_INT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val weeklyGoalKm: Double = HOME_ZERO_DOUBLE,
    val totalThisWeekKm: Double = HOME_ZERO_DOUBLE,
    val remainingKm: Float = HOME_ZERO_FLOAT,
    val recordCountThisWeek: Int = HOME_ZERO_INT,
    val progress: Float = HOME_ZERO_FLOAT,
    val activityLabelResId: Int? = null,
    val recentActivities: List<HomeRecentActivityUiModel> = emptyList()
)

sealed interface HomeUiEffect {
    data object NavigateToRecord : HomeUiEffect
    data object NavigateToGoal : HomeUiEffect
    data object NavigateToReminder : HomeUiEffect
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getRunningSummaryUseCase: GetRunningSummaryUseCase
) : ViewModel() {

    private val activityState = MutableStateFlow(ActivityRecognitionUiState())
    private val activityLogs = MutableStateFlow<List<ActivityLogUiModel>>(emptyList())
    private val recentActivityMaxCount = MutableStateFlow(HOME_ZERO_INT)
    private var activityStateJob: Job? = null
    private var activityLogsJob: Job? = null

    private val _effect = MutableSharedFlow<HomeUiEffect>()
    val effect = _effect.asSharedFlow()

    val uiState: StateFlow<HomeUiState> =
        combine(
            getRunningSummaryUseCase(),
            activityState,
            activityLogs,
            recentActivityMaxCount
        ) { summary, currentActivityState, logs, maxCount ->
            val weeklyGoalKm = summary.weeklyGoalKm ?: HOME_ZERO_DOUBLE
            val totalThisWeekKm = summary.totalThisWeekKm
            val remainingKm =
                (weeklyGoalKm - totalThisWeekKm).coerceAtLeast(HOME_ZERO_DOUBLE).toFloat()
            val recentActivities = if (maxCount > HOME_ZERO_INT) {
                logs.takeLast(maxCount)
                    .asReversed()
                    .map { log ->
                        val dateInfo = extractActivityDateInfo(log.time)
                        HomeRecentActivityUiModel(
                            timestamp = log.time,
                            month = dateInfo.month,
                            day = dateInfo.day,
                            dayOfWeek = dateInfo.dayOfWeek,
                            typeResId = log.labelResId
                        )
                    }
            } else {
                emptyList()
            }
            HomeUiState(
                weeklyGoalKm = weeklyGoalKm,
                totalThisWeekKm = totalThisWeekKm,
                remainingKm = remainingKm,
                recordCountThisWeek = summary.recordCountThisWeek,
                progress = summary.progress,
                activityLabelResId = currentActivityState.labelResId,
                recentActivities = recentActivities
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = HomeUiState()
        )

    fun bindActivityFlows(
        activityStateFlow: Flow<ActivityRecognitionUiState>,
        activityLogsFlow: Flow<List<ActivityLogUiModel>>,
        recentActivityMaxCount: Int
    ) {
        this.recentActivityMaxCount.value = recentActivityMaxCount
        activityStateJob?.cancel()
        activityLogsJob?.cancel()
        activityStateJob = viewModelScope.launch {
            activityStateFlow.collect { activityState.value = it }
        }
        activityLogsJob = viewModelScope.launch {
            activityLogsFlow.collect { activityLogs.value = it }
        }
    }

    fun onRecordClick() = emitEffect(HomeUiEffect.NavigateToRecord)

    fun onGoalClick() = emitEffect(HomeUiEffect.NavigateToGoal)

    fun onReminderClick() = emitEffect(HomeUiEffect.NavigateToReminder)

    private fun emitEffect(effect: HomeUiEffect) =
        viewModelScope.launch { _effect.emit(effect) }
}
