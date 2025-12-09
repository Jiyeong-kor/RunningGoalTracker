package com.jeong.runninggoaltracker.domain.model

data class RunningGoal(
    val weeklyGoalKm: Double
) {
    init {
        require(weeklyGoalKm > 0.0)
    }
}
