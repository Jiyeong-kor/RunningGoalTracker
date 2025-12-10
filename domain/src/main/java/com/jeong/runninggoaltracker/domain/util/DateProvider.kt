package com.jeong.runninggoaltracker.domain.util

import java.time.LocalDate

interface DateProvider {
    fun getToday(): LocalDate
}
