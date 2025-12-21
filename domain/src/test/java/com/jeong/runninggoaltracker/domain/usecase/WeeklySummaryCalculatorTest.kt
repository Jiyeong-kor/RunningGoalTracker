package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklySummaryCalculatorTest {

    private val calculator = WeeklySummaryCalculator()

    @Test
    fun `calculates totals for current week only`() {

        //금요일
        val today = LocalDate.of(2024, 11, 8)
        val goal = RunningGoal(weeklyGoalKm = 20.0)
        val records = listOf(
            RunningRecord(date = LocalDate.of(2024, 11, 4), distanceKm = 5.0, durationMinutes = 30),
            RunningRecord(date = LocalDate.of(2024, 11, 6), distanceKm = 7.0, durationMinutes = 40),
            // 저번주 기록은 무시돼야 함
            RunningRecord(date = LocalDate.of(2024, 11, 2), distanceKm = 10.0, durationMinutes = 60)
        )

        val summary = calculator.calculate(goal = goal, records = records, today = today)

        assertEquals(12.0, summary.totalThisWeekKm, 0.0)
        assertEquals(2, summary.recordCountThisWeek)
        assertEquals(0.6f, summary.progress)
        assertEquals(goal.weeklyGoalKm, summary.weeklyGoalKm)
    }

    @Test
    fun `returns zero progress when no goal is set`() {
        val today = LocalDate.of(2024, 1, 5)
        val records = listOf(
            RunningRecord(date = LocalDate.of(2024, 1, 1), distanceKm = 3.0, durationMinutes = 20),
            RunningRecord(date = LocalDate.of(2024, 1, 2), distanceKm = 2.0, durationMinutes = 15)
        )

        val summary = calculator.calculate(goal = null, records = records, today = today)

        assertEquals(5.0, summary.totalThisWeekKm, 0.0)
        assertEquals(2, summary.recordCountThisWeek)
        assertEquals(0f, summary.progress)
        assertEquals(null, summary.weeklyGoalKm)
    }

    @Test
    fun `caps progress at 100 percent`() {
        val today = LocalDate.of(2024, 3, 10)
        val goal = RunningGoal(weeklyGoalKm = 5.0)
        val records = listOf(
            RunningRecord(date = LocalDate.of(2024, 3, 4), distanceKm = 4.0, durationMinutes = 25),
            RunningRecord(date = LocalDate.of(2024, 3, 7), distanceKm = 3.0, durationMinutes = 18)
        )

        val summary = calculator.calculate(goal = goal, records = records, today = today)

        assertEquals(7.0, summary.totalThisWeekKm, 0.0)
        assertEquals(2, summary.recordCountThisWeek)
        assertEquals(1f, summary.progress)
    }
}
