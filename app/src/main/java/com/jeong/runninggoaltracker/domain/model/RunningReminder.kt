package com.jeong.runninggoaltracker.domain.model

data class RunningReminder(
    val hour: Int,
    val minute: Int,
    val enabled: Boolean
)
