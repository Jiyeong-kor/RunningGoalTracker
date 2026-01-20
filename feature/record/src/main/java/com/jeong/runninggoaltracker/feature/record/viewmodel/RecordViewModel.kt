package com.jeong.runninggoaltracker.feature.record.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.feature.record.presentation.RecordUiState
import com.jeong.runninggoaltracker.feature.record.presentation.RecordElapsedTimeUiState
import com.jeong.runninggoaltracker.feature.record.presentation.RecordPaceUiState
import com.jeong.runninggoaltracker.feature.record.contract.RECORD_MILLIS_PER_SECOND
import com.jeong.runninggoaltracker.feature.record.contract.RECORD_MINUTES_PER_HOUR
import com.jeong.runninggoaltracker.feature.record.contract.RECORD_SECONDS_PER_HOUR
import com.jeong.runninggoaltracker.feature.record.contract.RECORD_SECONDS_PER_MINUTE
import com.jeong.runninggoaltracker.feature.record.contract.RECORD_ZERO_DOUBLE
import com.jeong.runninggoaltracker.feature.record.contract.RECORD_ZERO_LONG
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionController
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.feature.record.api.RunningTrackerController
import com.jeong.runninggoaltracker.feature.record.api.RunningTrackerMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    getRunningRecordsUseCase: GetRunningRecordsUseCase,
    private val activityRecognitionController: ActivityRecognitionController,
    activityRecognitionMonitor: ActivityRecognitionMonitor,
    private val runningTrackerController: RunningTrackerController,
    runningTrackerMonitor: RunningTrackerMonitor
) : ViewModel() {

    val uiState: StateFlow<RecordUiState> = combine(
        getRunningRecordsUseCase(),
        activityRecognitionMonitor.activityState,
        runningTrackerMonitor.trackerState
    ) { records, activity, tracker ->
        RecordUiState(
            records = records,
            activityStatus = activity.status,
            isTracking = tracker.isTracking,
            distanceKm = tracker.distanceKm,
            elapsedMillis = tracker.elapsedMillis,
            elapsedTime = calculateElapsedTime(tracker.elapsedMillis),
            pace = calculatePace(tracker.distanceKm, tracker.elapsedMillis),
            permissionRequired = tracker.permissionRequired
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = RecordUiState()
    )

    fun startActivityRecognition() = activityRecognitionController.startUpdates()

    fun stopActivityRecognition() = activityRecognitionController.stopUpdates()

    fun startTracking() = runningTrackerController.startTracking()

    fun stopTracking() = runningTrackerController.stopTracking()

    private fun calculateElapsedTime(elapsedMillis: Long): RecordElapsedTimeUiState {
        val totalSeconds = elapsedMillis / RECORD_MILLIS_PER_SECOND
        val hours = totalSeconds / RECORD_SECONDS_PER_HOUR
        val minutes =
            (totalSeconds / RECORD_SECONDS_PER_MINUTE) % RECORD_MINUTES_PER_HOUR
        val seconds = totalSeconds % RECORD_SECONDS_PER_MINUTE
        return RecordElapsedTimeUiState(
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            showHours = hours > RECORD_ZERO_LONG
        )
    }

    private fun calculatePace(distanceKm: Double, elapsedMillis: Long): RecordPaceUiState {
        if (distanceKm <= RECORD_ZERO_DOUBLE || elapsedMillis <= RECORD_ZERO_LONG) {
            return RecordPaceUiState()
        }
        val totalSeconds = elapsedMillis / RECORD_MILLIS_PER_SECOND
        val secondsPerKm = totalSeconds.toDouble() / distanceKm
        val minutes = (secondsPerKm / RECORD_SECONDS_PER_MINUTE).toInt()
        val seconds = (secondsPerKm % RECORD_SECONDS_PER_MINUTE).toInt()
        return RecordPaceUiState(
            minutes = minutes,
            seconds = seconds,
            isAvailable = true
        )
    }
}
