package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRunningRemindersUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    operator fun invoke(): Flow<List<RunningReminder>> {
        return repository.getAllReminders()
            .map { list ->
                list.sortedBy { it.id }
            }
    }
}
