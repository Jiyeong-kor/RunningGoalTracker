package com.jeong.runninggoaltracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RunningRecordEntity::class,
        RunningGoalEntity::class,
        RunningReminderEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class RunningDatabase : RoomDatabase() {
    abstract fun runningDao(): RunningDao

    companion object {
        const val NAME = "running_goal_tracker.db"
    }
}
