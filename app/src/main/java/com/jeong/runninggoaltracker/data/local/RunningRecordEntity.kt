package com.jeong.runninggoaltracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_record")
data class RunningRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val date: String,          // "2025-11-28" 이런 형식으로 저장
    val distanceKm: Double,    // km
    val durationMinutes: Int   // 분
)
