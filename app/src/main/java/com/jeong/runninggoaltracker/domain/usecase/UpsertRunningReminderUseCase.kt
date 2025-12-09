package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import javax.inject.Inject

class UpsertRunningReminderUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    suspend operator fun invoke(reminder: RunningReminder) {
        repository.upsertReminder(reminder)
    }
}
