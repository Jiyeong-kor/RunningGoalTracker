package com.jeong.runninggoaltracker.data.local

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class RunningMappersTest {

    @Test
    fun `러닝 기록 엔티티를 도메인 모델로 변환하고 다시 엔티티로 복원`() {
        val entity = RunningRecordEntity(
            id = 10L,
            date = "2024-03-15",
            distanceKm = 7.5,
            durationMinutes = 42
        )

        val domain = entity.toDomain()
        val mappedEntity = domain.toEntity()

        assertEquals(LocalDate.of(2024, 3, 15), domain.date)
        assertEquals(entity, mappedEntity)
    }

    @Test
    fun `주간 목표를 엔티티와 도메인 모델 간에 일관되게 매핑`() {
        val goal = RunningGoal(weeklyGoalKm = 12.0)

        val entity = goal.toEntity()
        val mappedGoal = entity.toDomain()

        assertEquals(goal, mappedGoal)
    }

    @Test
    fun `러닝 알림을 엔티티와 도메인 모델 간에 일관되게 매핑`() {
        val reminder = RunningReminder(
            id = 3,
            hour = 6,
            minute = 30,
            enabled = true,
            days = setOf(DayOfWeek.SATURDAY, DayOfWeek.MONDAY)
        )

        val entity = reminder.toEntity()
        val mappedReminder = entity.toDomain()

        assertEquals(reminder.copy(days = reminder.days), mappedReminder)
        assertEquals("1,6", entity.days.split(",").sorted().joinToString(","))
    }
}
