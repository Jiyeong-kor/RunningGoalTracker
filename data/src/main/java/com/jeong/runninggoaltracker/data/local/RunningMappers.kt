package com.jeong.runninggoaltracker.data.local

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.model.RunningReminder

fun RunningRecordEntity.toDomain(): RunningRecord =
    RunningRecord(
        id = id,
        date = date,
        distanceKm = distanceKm,
        durationMinutes = durationMinutes
    )

fun RunningRecord.toEntity(): RunningRecordEntity =
    RunningRecordEntity(
        id = id,
        date = date,
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
        days = days
    )

fun RunningReminder.toEntity(): RunningReminderEntity =
    RunningReminderEntity(
        id = id,
        hour = hour,
        minute = minute,
        enabled = enabled,
        days = days
    )
