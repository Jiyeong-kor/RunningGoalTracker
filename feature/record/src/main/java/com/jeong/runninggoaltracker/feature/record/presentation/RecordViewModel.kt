package com.jeong.runninggoaltracker.feature.record.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.domain.util.DateFormatter
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionController
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.feature.record.tracking.RunningTrackerController
import com.jeong.runninggoaltracker.feature.record.tracking.RunningTrackerMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class RecordUiState(
    val records: List<RunningRecord> = emptyList(),
    val activityLabel: String = "UNKNOWN",
    val isTracking: Boolean = false,
    val distanceKm: Double = 0.0,
    val elapsedMillis: Long = 0L,
    val permissionRequired: Boolean = false
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    val dateFormatter: DateFormatter,
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
}
