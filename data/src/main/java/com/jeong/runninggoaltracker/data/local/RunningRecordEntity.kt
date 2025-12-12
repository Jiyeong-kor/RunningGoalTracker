package com.jeong.runninggoaltracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_record")
data class RunningRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: String,
    val distanceKm: Double,
    val durationMinutes: Int
)
