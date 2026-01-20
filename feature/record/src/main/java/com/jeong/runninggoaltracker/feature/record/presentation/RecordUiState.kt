package com.jeong.runninggoaltracker.feature.record.presentation

import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus
import com.jeong.runninggoaltracker.domain.model.RunningRecord

data class RecordElapsedTimeUiState(
    val hours: Long = 0L,
    val minutes: Long = 0L,
    val seconds: Long = 0L,
    val showHours: Boolean = false
)

data class RecordPaceUiState(
    val minutes: Int = 0,
    val seconds: Int = 0,
    val isAvailable: Boolean = false
)

data class RecordUiState(
    val records: List<RunningRecord> = emptyList(),
    val activityStatus: ActivityRecognitionStatus = ActivityRecognitionStatus.Unknown,
    val isTracking: Boolean = false,
    val distanceKm: Double = 0.0,
    val elapsedMillis: Long = 0L,
    val elapsedTime: RecordElapsedTimeUiState = RecordElapsedTimeUiState(),
    val pace: RecordPaceUiState = RecordPaceUiState(),
    val permissionRequired: Boolean = false
)
