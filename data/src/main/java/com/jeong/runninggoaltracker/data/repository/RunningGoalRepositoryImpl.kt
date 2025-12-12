package com.jeong.runninggoaltracker.data.repository

import com.jeong.runninggoaltracker.data.local.RunningDao
import com.jeong.runninggoaltracker.data.local.toDomain
import com.jeong.runninggoaltracker.data.local.toEntity
import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.repository.RunningGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RunningGoalRepositoryImpl @Inject constructor(
    private val dao: RunningDao
) : RunningGoalRepository {

    override fun getGoal(): Flow<RunningGoal?> = dao.getGoal().map { it?.toDomain() }

    override suspend fun upsertGoal(goal: RunningGoal) {
        dao.upsertGoal(goal.toEntity())
    }
}
