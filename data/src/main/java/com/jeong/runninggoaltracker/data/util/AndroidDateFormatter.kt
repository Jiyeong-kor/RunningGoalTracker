package com.jeong.runninggoaltracker.data.util

import android.os.Build
import com.jeong.runninggoaltracker.domain.util.DateFormatter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class AndroidDateFormatter @Inject constructor() : DateFormatter {

    override fun formatToKoreanDate(timestamp: Long): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN)
            Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
        } else {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = timestamp
            }
            String.format(
                Locale.KOREAN,
                "%d년 %02d월 %02d일",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
            )
        }
    }

    override fun formatToDistanceLabel(distanceKm: Double): String {
        return String.format(Locale.getDefault(), "%.2f km", distanceKm)
    }

    override fun formatElapsedTime(elapsedMillis: Long): String {
        val totalSeconds = elapsedMillis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
