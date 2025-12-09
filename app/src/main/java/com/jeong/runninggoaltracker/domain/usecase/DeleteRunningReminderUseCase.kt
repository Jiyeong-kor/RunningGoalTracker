package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import javax.inject.Inject

class DeleteRunningReminderUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    suspend operator fun invoke(reminderId: Int) {
        repository.deleteReminder(reminderId)
    }
}
