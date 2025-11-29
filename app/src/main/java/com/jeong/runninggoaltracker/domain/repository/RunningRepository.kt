package com.jeong.runninggoaltracker.domain.repository

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import kotlinx.coroutines.flow.Flow

interface RunningRepository {

    fun getAllRecords(): Flow<List<RunningRecord>>
    suspend fun addRecord(record: RunningRecord)

    fun getGoal(): Flow<RunningGoal?>
    suspend fun upsertGoal(goal: RunningGoal)

    fun getReminder(): Flow<RunningReminder?>
    suspend fun upsertReminder(reminder: RunningReminder)
}
