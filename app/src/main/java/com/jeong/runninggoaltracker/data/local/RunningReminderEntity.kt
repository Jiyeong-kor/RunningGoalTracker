package com.jeong.runninggoaltracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "running_reminder")
data class RunningReminderEntity(
    @PrimaryKey val id: Int = 0, // 단일 레코드
    val hour: Int,
    val minute: Int,
    val enabled: Boolean
)
