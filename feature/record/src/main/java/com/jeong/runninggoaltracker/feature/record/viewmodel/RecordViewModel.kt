package com.jeong.runninggoaltracker.feature.record.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.domain.util.DateFormatter
import com.jeong.runninggoaltracker.feature.record.presentation.RecordUiState
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
    private val dateFormatter: DateFormatter,
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
            activityLabel = activity.label,
            isTracking = tracker.isTracking,
            distanceKm = tracker.distanceKm,
            elapsedMillis = tracker.elapsedMillis,
            permissionRequired = tracker.permissionRequired
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = RecordUiState()
    )

    fun startActivityRecognition(onPermissionRequired: () -> Unit) {
        activityRecognitionController.startUpdates(onPermissionRequired)
    }

    fun stopActivityRecognition() {
        activityRecognitionController.stopUpdates()
    }

    fun notifyPermissionDenied() {
        activityRecognitionController.notifyPermissionDenied()
    }

    fun startTracking(onPermissionRequired: () -> Unit) {
        runningTrackerController.startTracking(onPermissionRequired)
    }

    fun stopTracking() {
        runningTrackerController.stopTracking()
    }

    fun notifyTrackingPermissionDenied() {
        runningTrackerController.notifyPermissionDenied()
    }

    fun formatToKoreanDate(timestamp: Long): String = dateFormatter.formatToKoreanDate(timestamp)

    fun formatToDistanceLabel(distanceKm: Double): String =
        dateFormatter.formatToDistanceLabel(distanceKm)

    fun formatElapsedTime(elapsedMillis: Long): String =
        dateFormatter.formatElapsedTime(elapsedMillis)
}
