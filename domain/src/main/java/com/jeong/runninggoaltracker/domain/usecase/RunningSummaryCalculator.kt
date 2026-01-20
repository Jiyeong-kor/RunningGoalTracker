package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.model.RunningSummary
import com.jeong.runninggoaltracker.domain.util.DateProvider
import javax.inject.Inject

private const val MIN_WEEKLY_GOAL_KM = 0.0
private const val MIN_PROGRESS_BOUND = 0.0
private const val MAX_PROGRESS_BOUND = 1.0
private const val ZERO_PROGRESS = 0f

interface RunningSummaryCalculator {
    fun calculate(
        goal: RunningGoal?,
        records: List<RunningRecord>,
        todayMillis: Long
    ): RunningSummary
}

class WeeklySummaryCalculator @Inject constructor(private val dateProvider: DateProvider) :
    RunningSummaryCalculator {

    override fun calculate(
        goal: RunningGoal?,
        records: List<RunningRecord>,
        todayMillis: Long
    ): RunningSummary {
        val startOfWeek = dateProvider.getStartOfWeek(todayMillis)
        val thisWeekRecords = records.filter { record ->
            record.date >= startOfWeek
        }

        val totalKm = thisWeekRecords.sumOf { it.distanceKm }
        val count = thisWeekRecords.size
        val weeklyGoalKm = goal?.weeklyGoalKm

        val progress = if (weeklyGoalKm != null && weeklyGoalKm > MIN_WEEKLY_GOAL_KM) {
            (totalKm / weeklyGoalKm)
                .coerceIn(MIN_PROGRESS_BOUND, MAX_PROGRESS_BOUND)
                .toFloat()
        } else {
            ZERO_PROGRESS
        }

        return RunningSummary(
            weeklyGoalKm = weeklyGoalKm,
            totalThisWeekKm = totalKm,
            recordCountThisWeek = count,
            progress = progress
        )
    }
}
