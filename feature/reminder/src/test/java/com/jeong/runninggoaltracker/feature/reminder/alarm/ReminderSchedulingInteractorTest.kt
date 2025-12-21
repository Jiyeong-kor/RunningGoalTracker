package com.jeong.runninggoaltracker.feature.reminder.alarm

import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.repository.RunningReminderRepository
import com.jeong.runninggoaltracker.domain.usecase.DeleteRunningReminderUseCase
import com.jeong.runninggoaltracker.domain.usecase.UpsertRunningReminderUseCase
import java.time.DayOfWeek
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulingInteractorTest {

    private val repository = FakeRunningReminderRepository()
    private val scheduler = FakeReminderScheduler()
    private val interactor = ReminderSchedulingInteractor(
        upsertRunningReminderUseCase = UpsertRunningReminderUseCase(repository),
        deleteRunningReminderUseCase = DeleteRunningReminderUseCase(repository),
        reminderScheduler = scheduler
    )

    @Test
    fun `세부 정보가 변경될 때 이전 알림을 취소하고 새 알림을 예약`() = runBlocking {
        val previous = RunningReminder(
            id = 1,
            hour = 7,
            minute = 0,
            enabled = true,
            days = setOf(DayOfWeek.MONDAY)
        )
        val updated = previous.copy(hour = 6, minute = 30, days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY))

        interactor.saveReminder(
            updatedReminder = updated,
            previousReminder = previous
        )

        assertEquals(listOf(updated), repository.saved)
        assertEquals(listOf(previous), scheduler.cancelled)
        assertEquals(listOf(updated), scheduler.scheduled)
    }

    @Test
    fun `알림 내용이 변경되지 않았을 때 다시 예약하지 않음`() = runBlocking {
        val reminder = RunningReminder(
            id = 2,
            hour = 8,
            minute = 15,
            enabled = true,
            days = setOf(DayOfWeek.TUESDAY)
        )

        interactor.saveReminder(
            updatedReminder = reminder,
            previousReminder = reminder
        )

        assertEquals(listOf(reminder), repository.saved)
        assertTrue(scheduler.cancelled.isEmpty())
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `알림이 비활성화되었거나 설정된 요일이 없을 때 예약을 건너뜀`() = runBlocking {
        val disabledReminder = RunningReminder(
            id = 3,
            hour = 9,
            minute = 45,
            enabled = false,
            days = setOf(DayOfWeek.WEDNESDAY)
        )
        val noDaysReminder = RunningReminder(
            id = 4,
            hour = 9,
            minute = 45,
            enabled = true,
            days = emptySet()
        )

        interactor.saveReminder(
            updatedReminder = disabledReminder,
            previousReminder = null
        )
        interactor.saveReminder(
            updatedReminder = noDaysReminder,
            previousReminder = null
        )

        assertEquals(listOf(disabledReminder, noDaysReminder), repository.saved)
        assertTrue(scheduler.cancelled.isEmpty())
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun `ID가 존재할 때만 예약된 알림을 취소`() = runBlocking {
        val savedReminder = RunningReminder(
            id = 5,
            hour = 10,
            minute = 0,
            enabled = true,
            days = setOf(DayOfWeek.SATURDAY)
        )
        val unsavedReminder = savedReminder.copy(id = null)

        interactor.deleteReminder(savedReminder)
        interactor.deleteReminder(unsavedReminder)

        assertEquals(listOf(savedReminder), scheduler.cancelled)
        assertEquals(listOf(5), repository.deletedIds)
    }

    private class FakeRunningReminderRepository : RunningReminderRepository {
        private val state = MutableStateFlow<List<RunningReminder>>(emptyList())
        val saved = mutableListOf<RunningReminder>()
        val deletedIds = mutableListOf<Int>()

        override fun getAllReminders(): Flow<List<RunningReminder>> = state

        override suspend fun upsertReminder(reminder: RunningReminder) {
            saved += reminder
            state.value = state.value.filterNot { it.id == reminder.id } + reminder
        }

        override suspend fun deleteReminder(reminderId: Int) {
            deletedIds += reminderId
            state.value = state.value.filterNot { it.id == reminderId }
        }
    }

    private class FakeReminderScheduler : ReminderScheduler {
        val scheduled = mutableListOf<RunningReminder>()
        val cancelled = mutableListOf<RunningReminder>()

        override fun scheduleIfNeeded(reminder: RunningReminder) {
            scheduled += reminder
        }

        override fun cancel(reminder: RunningReminder) {
            cancelled += reminder
        }
    }
}
