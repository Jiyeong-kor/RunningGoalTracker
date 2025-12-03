package com.jeong.runninggoaltracker.presentation.common

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
fun String.toKoreanDateLabel(): String {
    return try {
        val parsed = LocalDate.parse(this) // "yyyy-MM-dd" 기준
        val formatter = DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREA)
        parsed.format(formatter)
    } catch (_: Exception) {
        this // 파싱 실패하면 원문 그대로
    }
}

fun Double.toDistanceLabel(): String {
    return if (this % 1.0 == 0.0) {
        String.format(Locale.getDefault(), "%.0f km", this)
    } else {
        String.format(Locale.getDefault(), "%.1f km", this)
    }
}
