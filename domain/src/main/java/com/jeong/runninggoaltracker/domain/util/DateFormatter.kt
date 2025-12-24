package com.jeong.runninggoaltracker.domain.util

interface DateFormatter {
    fun formatToKoreanDate(timestamp: Long): String
    fun formatToDistanceLabel(distanceKm: Double): String
    fun formatElapsedTime(elapsedMillis: Long): String
}
