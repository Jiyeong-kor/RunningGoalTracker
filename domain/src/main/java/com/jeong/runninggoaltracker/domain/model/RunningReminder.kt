package com.jeong.runninggoaltracker.domain.model

data class RunningReminder(
    val id: Int? = null,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val days: Set<Int>
) {
    fun toggleDay(day: Int): RunningReminder {
        val newDays = if (days.contains(day)) {
            days - day
        } else {
            days + day
        }
        return this.copy(days = newDays)
    }
}
