package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningReminder
import org.junit.Assert.assertEquals
import org.junit.Test

class ToggleReminderDayUseCaseTest {

    private val useCase = ToggleReminderDayUseCase()

    @Test
    fun addsDayWhenMissing() {
        val reminder = RunningReminder(
            id = 1,
            hour = 6,
            minute = 30,
            enabled = true,
            days = setOf(1)
        )

        val result = useCase(reminder, 2)

        assertEquals(setOf(1, 2), result.days)
    }

    @Test
    fun removesDayWhenPresent() {
        val reminder = RunningReminder(
            id = 1,
            hour = 6,
            minute = 30,
            enabled = true,
            days = setOf(1, 2)
        )

        val result = useCase(reminder, 2)

        assertEquals(setOf(1), result.days)
    }
}
