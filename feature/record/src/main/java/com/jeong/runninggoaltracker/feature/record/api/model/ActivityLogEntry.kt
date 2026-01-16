package com.jeong.runninggoaltracker.feature.record.api.model

data class ActivityLogEntry(
    val time: Long,
    val status: ActivityRecognitionStatus
)
