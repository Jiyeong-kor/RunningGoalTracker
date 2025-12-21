package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.repository.RunningGoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class UpsertRunningGoalUseCaseTest {

    private val repository = FakeRunningGoalRepository()
    private val useCase = UpsertRunningGoalUseCase(repository)

    @Test
    fun `거리가 양수일 때 목표를 저장`() = runBlocking {
        val goal = RunningGoal(weeklyGoalKm = 15.0)

        useCase(goal)

        assertEquals(goal, repository.savedGoal)
    }

    @Test
    fun `목표 거리가 양수가 아닐 때 예외를 발생`() {
        val goal = RunningGoal(weeklyGoalKm = 0.0)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { useCase(goal) }
        }
    }

    private class FakeRunningGoalRepository : RunningGoalRepository {
        var savedGoal: RunningGoal? = null
        private val goalFlow = MutableStateFlow<RunningGoal?>(null)

        override fun getGoal(): Flow<RunningGoal?> = goalFlow

        override suspend fun upsertGoal(goal: RunningGoal) {
            savedGoal = goal
            goalFlow.value = goal
        }
    }
}
