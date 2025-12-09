package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import java.time.DayOfWeek
import java.time.LocalDate

interface RunningSummaryCalculator {
    fun calculate(
        goal: RunningGoal?,
        records: List<RunningRecord>,
        today: LocalDate
    ): RunningSummary
}

class WeeklySummaryCalculator : RunningSummaryCalculator {
    override fun calculate(
        goal: RunningGoal?,
        records: List<RunningRecord>,
        today: LocalDate
    ): RunningSummary {
        val startOfWeek = today.with(DayOfWeek.MONDAY)

        val thisWeekRecords = records.filter { record ->
            !record.date.isBefore(startOfWeek)
        }

        val totalKm = thisWeekRecords.sumOf { it.distanceKm }
        val count = thisWeekRecords.size
        val weeklyGoalKm = goal?.weeklyGoalKm

        val progress = if (weeklyGoalKm != null && weeklyGoalKm > 0.0) {
            (totalKm / weeklyGoalKm)
                .coerceIn(0.0, 1.0)
                .toFloat()
        } else {
            0f
        }

        return RunningSummary(
            weeklyGoalKm = weeklyGoalKm,
            totalThisWeekKm = totalKm,
            recordCountThisWeek = count,
            progress = progress
        )
    }
}
