package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import javax.inject.Inject

class AddRunningRecordUseCase @Inject constructor(
    private val repository: RunningRecordRepository
) {
    suspend operator fun invoke(date: Long, distanceKm: Double, durationMinutes: Int) {
        repository.addRecord(
            RunningRecord(
                date = date,
                distanceKm = distanceKm,
                durationMinutes = durationMinutes
            )
        )
    }
}
