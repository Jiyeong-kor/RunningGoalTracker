package com.jeong.runninggoaltracker.data.mock

import com.jeong.runninggoaltracker.data.local.RunningGoalEntity
import com.jeong.runninggoaltracker.data.local.RunningRecordEntity
import com.jeong.runninggoaltracker.data.local.RunningReminderEntity
import com.jeong.runninggoaltracker.data.local.WorkoutRecordEntity
import com.jeong.runninggoaltracker.domain.model.ExerciseType
import java.util.Calendar

object RunningMockData {
    private const val MOCK_BASE_TIME_MILLIS = 1_726_214_400_000L
    private const val MOCK_DAY_MILLIS = 86_400_000L

    fun runningGoalEntity(): RunningGoalEntity = RunningGoalEntity(weeklyGoalKm = 12.5)

    fun runningRecordEntities(baseTimeMillis: Long = MOCK_BASE_TIME_MILLIS): List<RunningRecordEntity> =
        listOf(
            RunningRecordEntity(
                id = 1L,
                date = baseTimeMillis,
                distanceKm = 5.2,
                durationMinutes = 32
            ),
            RunningRecordEntity(
                id = 2L,
                date = baseTimeMillis - MOCK_DAY_MILLIS,
                distanceKm = 3.4,
                durationMinutes = 21
            ),
            RunningRecordEntity(
                id = 3L,
                date = baseTimeMillis - (MOCK_DAY_MILLIS * 2),
                distanceKm = 7.1,
                durationMinutes = 48
            )
        )

    fun runningReminderEntity(): RunningReminderEntity = RunningReminderEntity(
        id = 1,
        hour = 7,
        minute = 30,
        enabled = true,
        days = setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
    )

    fun workoutRecordEntities(baseTimeMillis: Long = MOCK_BASE_TIME_MILLIS): List<WorkoutRecordEntity> =
        listOf(
            WorkoutRecordEntity(
                date = baseTimeMillis,
                exerciseType = ExerciseType.SQUAT.name,
                repCount = 12
            ),
            WorkoutRecordEntity(
                date = baseTimeMillis - (MOCK_DAY_MILLIS * 2),
                exerciseType = ExerciseType.LUNGE.name,
                repCount = 10
            ),
            WorkoutRecordEntity(
                date = baseTimeMillis - (MOCK_DAY_MILLIS * 4),
                exerciseType = ExerciseType.PUSH_UP.name,
                repCount = 15
            )
        )
}
