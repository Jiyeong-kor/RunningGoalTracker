package com.jeong.runninggoaltracker.util

import com.jeong.runninggoaltracker.domain.util.DateProvider
import java.time.LocalDate

class SystemDateProvider : DateProvider {
    override fun getToday(): LocalDate = LocalDate.now()
}
