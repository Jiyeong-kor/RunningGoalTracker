package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import javax.inject.Inject

class UpsertRunningGoalUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    suspend operator fun invoke(weeklyGoalKm: Double) {
        repository.upsertGoal(
            RunningGoal(weeklyGoalKm = weeklyGoalKm)
        )
    }
}
