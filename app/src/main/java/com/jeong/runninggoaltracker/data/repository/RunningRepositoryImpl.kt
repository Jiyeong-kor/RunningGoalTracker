package com.jeong.runninggoaltracker.data.repository

import com.jeong.runninggoaltracker.data.local.RunningDao
import com.jeong.runninggoaltracker.data.local.toDomain
import com.jeong.runninggoaltracker.data.local.toEntity
import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RunningRepositoryImpl(
    private val dao: RunningDao
) : RunningRepository {

    override fun getAllRecords(): Flow<List<RunningRecord>> =
        dao.getAllRecords()
            .map { list -> list.map { it.toDomain() } }

    override suspend fun addRecord(record: RunningRecord) {
        dao.insertRecord(record.toEntity())
    }

    override fun getGoal(): Flow<RunningGoal?> =
        dao.getGoal().map { entity -> entity?.toDomain() }

    override suspend fun upsertGoal(goal: RunningGoal) {
        dao.upsertGoal(goal.toEntity())
    }

    override fun getReminder(): Flow<RunningReminder?> =
        dao.getReminder().map { it?.toDomain() }

    override suspend fun upsertReminder(reminder: RunningReminder) {
        dao.upsertReminder(reminder.toEntity())
    }
}
