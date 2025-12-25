package com.jeong.runninggoaltracker.feature.record.api.model

data class RunningTrackerState(
    val isTracking: Boolean = false,
    val distanceKm: Double = 0.0,
    val elapsedMillis: Long = 0L,
    val permissionRequired: Boolean = false
)
