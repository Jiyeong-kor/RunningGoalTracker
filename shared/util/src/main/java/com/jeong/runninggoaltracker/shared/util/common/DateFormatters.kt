package com.jeong.runninggoaltracker.shared.util.common

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate.toKoreanDateLabel(): String = try {
    val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREA)
    this.format(formatter)
} catch (_: Exception) {
    this.toString()
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toKoreanDateLabel(): String = try {
    val parsed = LocalDate.parse(this)
    val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREA)
    parsed.format(formatter)
} catch (_: Exception) {
    this
}

fun Double.toDistanceLabel(): String = if (this % 1.0 == 0.0) {
    String.format(Locale.getDefault(), "%.0f km", this)
} else {
    String.format(Locale.getDefault(), "%.1f km", this)
}
