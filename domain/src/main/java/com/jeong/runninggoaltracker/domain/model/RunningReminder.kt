package com.jeong.runninggoaltracker.domain.model

import java.time.DayOfWeek

data class RunningReminder(
    val id: Int? = null,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val days: Set<DayOfWeek> = emptySet()
) {
    init {
        require(hour in 0..23)
        require(minute in 0..59)
    }
}
