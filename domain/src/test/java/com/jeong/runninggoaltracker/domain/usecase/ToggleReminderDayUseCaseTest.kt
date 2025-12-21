package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningReminder
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleReminderDayUseCaseTest {

    private lateinit var useCase: ToggleReminderDayUseCase

    @Before
    fun setUp() {
        useCase = ToggleReminderDayUseCase()
    }

    @Test
    fun `adds day when it is not present`() {
        val reminder = RunningReminder(
            hour = 7,
            minute = 30,
            enabled = true,
            days = emptySet()
        )

        val updated = useCase(reminder, DayOfWeek.MONDAY.value)

        assertTrue(updated.days.contains(DayOfWeek.MONDAY))
    }

    @Test
    fun `removes day when it is already present`() {
        val reminder = RunningReminder(
            hour = 7,
            minute = 30,
            enabled = true,
            days = setOf(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        )

        val updated = useCase(reminder, DayOfWeek.FRIDAY.value)

        assertFalse(updated.days.contains(DayOfWeek.FRIDAY))
        assertTrue(updated.days.contains(DayOfWeek.WEDNESDAY))
    }

    @Test
    fun `returns original reminder when day is invalid`() {
        val reminder = RunningReminder(
            hour = 6,
            minute = 0,
            enabled = false,
            days = setOf(DayOfWeek.SATURDAY)
        )

        val updated = useCase(reminder, 8)

        assertEquals(reminder, updated)
    }
}
