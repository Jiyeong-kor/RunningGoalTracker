package com.jeong.runninggoaltracker.data.local

import androidx.room.TypeConverter

private const val DAYS_DELIMITER = ","

class DayOfWeekConverter {
    @TypeConverter
    fun toDays(days: Set<Int>?): String =
        days?.joinToString(DAYS_DELIMITER) ?: EMPTY_DAYS

    @TypeConverter
    fun fromDays(value: String?): Set<Int> =
        value?.split(DAYS_DELIMITER)
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet() ?: emptySet()
}
