package com.jeong.runninggoaltracker.domain.usecase

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateDefaultReminderUseCaseTest {

    private val useCase = CreateDefaultReminderUseCase()

    @Test
    fun `선택된 요일 없이 비활성화된 아침 알림을 제공`() {
        val reminder = useCase()

        assertEquals(null, reminder.id)
        assertEquals(8, reminder.hour)
        assertEquals(0, reminder.minute)
        assertEquals(false, reminder.enabled)
        assertEquals(emptySet<DayOfWeek>(), reminder.days)
    }
}
