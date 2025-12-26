package com.jeong.runninggoaltracker.data.repository

import com.jeong.runninggoaltracker.data.local.RunningRecordDao
import com.jeong.runninggoaltracker.data.local.toDomain
import com.jeong.runninggoaltracker.data.local.toEntity
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RunningRecordRepositoryImpl @Inject constructor(
    private val recordDao: RunningRecordDao
) : RunningRecordRepository {

    override fun getAllRecords(): Flow<List<RunningRecord>> {
        return recordDao.getAllRecords().map { records ->
            records.map { it.toDomain() }
        }
    }

    override suspend fun addRecord(record: RunningRecord) {
        recordDao.insertRecord(record.toEntity())
    }
}
