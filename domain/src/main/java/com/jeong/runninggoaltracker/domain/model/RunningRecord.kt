package com.jeong.runninggoaltracker.domain.model

data class RunningRecord(
    val id: Long = 0L,
    val date: Long,
    val distanceKm: Double,
    val durationMinutes: Int
)
