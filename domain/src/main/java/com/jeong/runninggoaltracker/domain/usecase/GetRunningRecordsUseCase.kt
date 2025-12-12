package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRunningRecordsUseCase(
    private val repository: RunningRecordRepository
) {
    operator fun invoke(): Flow<List<RunningRecord>> =
        repository.getAllRecords().map { records ->
            records.sortedByDescending { it.date }
        }
}
