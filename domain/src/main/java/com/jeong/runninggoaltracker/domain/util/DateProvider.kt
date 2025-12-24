package com.jeong.runninggoaltracker.domain.util

import kotlinx.coroutines.flow.Flow

interface DateProvider {
    fun getTodayFlow(): Flow<Long>
    fun getToday(): Long
    fun getStartOfWeek(timestamp: Long): Long
}
