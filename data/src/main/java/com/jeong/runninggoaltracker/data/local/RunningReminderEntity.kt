package com.jeong.runninggoaltracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

const val EMPTY_DAYS = ""
const val TABLE_RUNNING_REMINDER = "running_reminder"

@Entity(tableName = TABLE_RUNNING_REMINDER)
data class RunningReminderEntity(
    @PrimaryKey val id: Int? = null,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean,
    val days: String = EMPTY_DAYS
)
