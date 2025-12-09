package com.jeong.runninggoaltracker.data.local

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import java.time.DayOfWeek
import java.time.LocalDate

private const val DAYS_DELIMITER = ","

fun RunningRecordEntity.toDomain(): RunningRecord =
    RunningRecord(
        id = id,
        date = LocalDate.parse(date),
        distanceKm = distanceKm,
        durationMinutes = durationMinutes
    )

fun RunningRecord.toEntity(): RunningRecordEntity =
    RunningRecordEntity(
        id = id,
        date = date.toString(),
        distanceKm = distanceKm,
        durationMinutes = durationMinutes
    )

fun RunningGoalEntity.toDomain(): RunningGoal =
    RunningGoal(
        weeklyGoalKm = weeklyGoalKm
    )

fun RunningGoal.toEntity(): RunningGoalEntity =
    RunningGoalEntity(
        weeklyGoalKm = weeklyGoalKm
    )

fun RunningReminderEntity.toDomain(): RunningReminder =
    RunningReminder(
        id = id,
        hour = hour,
        minute = minute,
        enabled = enabled,
        days = days.split(DAYS_DELIMITER)
            .filter { it.isNotBlank() }
            .mapNotNull { it.toIntOrNull() }
            .mapNotNull { dayInt ->
                runCatching { DayOfWeek.of(dayInt) }.getOrNull()
            }.toSet()
    )

fun RunningReminder.toEntity(): RunningReminderEntity =
    RunningReminderEntity(
        id = id,
        hour = hour,
        minute = minute,
        enabled = enabled,
        days = days.map { it.value }.joinToString(DAYS_DELIMITER)
    )
