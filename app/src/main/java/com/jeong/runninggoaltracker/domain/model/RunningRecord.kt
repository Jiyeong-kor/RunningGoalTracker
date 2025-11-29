package com.jeong.runninggoaltracker.domain.model

data class RunningRecord(
    val id: Long = 0L,
    val date: String,          // "2025-11-28" 같은 형식
    val distanceKm: Double,    // km
    val durationMinutes: Int   // 분
)
