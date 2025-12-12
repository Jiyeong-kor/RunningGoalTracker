package com.jeong.runninggoaltracker.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.DayOfWeek
import java.time.LocalDate

private const val DAYS_DELIMITER = ","

class LocalDateConverter {
    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): String? = date?.toString()


    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromTimestamp(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

}

class DayOfWeekConverter {
    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toDays(days: Set<DayOfWeek>?): String =
        days?.joinToString(DAYS_DELIMITER) { it.value.toString() } ?: EMPTY_DAYS

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromDays(value: String?): Set<DayOfWeek> = value?.split(DAYS_DELIMITER)
        ?.filter { it.isNotBlank() }
        ?.mapNotNull { it.toIntOrNull() }
        ?.mapNotNull { dayInt ->
            runCatching { DayOfWeek.of(dayInt) }.getOrNull()
        }
        ?.toSet() ?: emptySet()
}
