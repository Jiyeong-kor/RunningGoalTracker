package com.jeong.runninggoaltracker.domain.contract

import java.util.Calendar

object DateTimeContract {
    const val WEEK_START_DAY: Int = Calendar.SUNDAY
    const val DAYS_IN_WEEK: Int = 7
    const val WEEK_END_OFFSET_DAYS: Int = DAYS_IN_WEEK - 1
}
