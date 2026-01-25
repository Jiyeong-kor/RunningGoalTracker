package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.util.DateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklySummaryCalculatorTest {
    private val dateProvider = object : DateProvider {
        override fun getTodayFlow(): Flow<Long> = flowOf(0L)

        override fun getToday(): Long = 0L

        override fun getStartOfWeek(timestamp: Long): Long = 1000L
    }

    private val calculator = WeeklySummaryCalculator(dateProvider)

    @Test
    fun calculateFiltersRecordsWithinWeekAndBuildsProgress() {
        val goal = RunningGoal(weeklyGoalKm = 10.0)
        val records = listOf(
            RunningRecord(id = 1L, date = 500L, distanceKm = 2.0, durationMinutes = 10),
            RunningRecord(id = 2L, date = 1200L, distanceKm = 3.0, durationMinutes = 20),
            RunningRecord(id = 3L, date = 1500L, distanceKm = 4.0, durationMinutes = 30)
        )

        val summary = calculator.calculate(goal, records, todayMillis = 1600L)

        assertEquals(10.0, summary.weeklyGoalKm ?: 0.0, 0.0)
        assertEquals(7.0, summary.totalThisWeekKm, 0.0)
        assertEquals(2, summary.recordCountThisWeek)
        assertEquals(0.7f, summary.progress, 0.0001f)
    }

    @Test
    fun calculateWithoutGoalReturnsZeroProgress() {
        val records = listOf(
            RunningRecord(id = 1L, date = 1200L, distanceKm = 3.0, durationMinutes = 20)
        )

        val summary = calculator.calculate(goal = null, records = records, todayMillis = 1600L)

        assertEquals(null, summary.weeklyGoalKm)
        assertEquals(3.0, summary.totalThisWeekKm, 0.0)
        assertEquals(1, summary.recordCountThisWeek)
        assertEquals(0f, summary.progress, 0.0001f)
    }
}
