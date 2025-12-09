package com.jeong.runninggoaltracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_goal")
data class RunningGoalEntity(
    @PrimaryKey val id: Int = 0,
    val weeklyGoalKm: Double
)
