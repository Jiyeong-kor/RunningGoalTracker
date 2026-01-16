package com.jeong.runninggoaltracker.feature.record.presentation

import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus
import com.jeong.runninggoaltracker.domain.model.RunningRecord

data class RecordUiState(
    val records: List<RunningRecord> = emptyList(),
    val activityStatus: ActivityRecognitionStatus = ActivityRecognitionStatus.Unknown,
    val isTracking: Boolean = false,
    val distanceKm: Double = 0.0,
    val elapsedMillis: Long = 0L,
    val permissionRequired: Boolean = false
)
