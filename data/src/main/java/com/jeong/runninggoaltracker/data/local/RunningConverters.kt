package com.jeong.runninggoaltracker.data.local

import androidx.room.TypeConverter
import com.jeong.runninggoaltracker.data.contract.RunningDatabaseContract

class DayOfWeekConverter {
    @TypeConverter
    fun toDays(days: Set<Int>?): String =
        days?.joinToString(RunningDatabaseContract.DAYS_DELIMITER)
            ?: RunningDatabaseContract.EMPTY_DAYS

    @TypeConverter
    fun fromDays(value: String?): Set<Int> =
        value?.split(RunningDatabaseContract.DAYS_DELIMITER)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet() ?: emptySet()
}
