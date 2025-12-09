package com.jeong.runninggoaltracker.domain.model

import java.time.LocalDate

data class RunningRecord(
    val id: Long = 0L,
    val date: LocalDate,
    val distanceKm: Double,
    val durationMinutes: Int
) {
    init {
        require(distanceKm > 0.0)
        require(durationMinutes > 0)
    }
}
