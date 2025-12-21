package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.repository.RunningReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteRunningReminderUseCaseTest {

    private val repository = FakeRunningReminderRepository()
    private val useCase = DeleteRunningReminderUseCase(repository)

    @Test
    fun `알림 ID를 레포지토리에 전달`() = runBlocking {
        useCase(reminderId = 12)

        assertEquals(12, repository.deletedId)
    }

    private class FakeRunningReminderRepository : RunningReminderRepository {
        var deletedId: Int? = null

        override fun getAllReminders(): Flow<List<RunningReminder>> = emptyFlow()

        override suspend fun upsertReminder(reminder: RunningReminder) = Unit

        override suspend fun deleteReminder(reminderId: Int) {
            deletedId = reminderId
        }
    }
}
