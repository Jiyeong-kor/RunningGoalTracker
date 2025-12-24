package com.jeong.runninggoaltracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RunningRecordEntity::class,
        RunningGoalEntity::class,
        RunningReminderEntity::class
    ],
    version = 5,
    exportSchema = false
)

@TypeConverters(
    DayOfWeekConverter::class
)

abstract class RunningDatabase : RoomDatabase() {
    abstract fun runningRecordDao(): RunningRecordDao
    abstract fun runningGoalDao(): RunningGoalDao
    abstract fun runningReminderDao(): RunningReminderDao

    companion object {
        const val NAME = "running_goal_tracker.db"
    }
}
