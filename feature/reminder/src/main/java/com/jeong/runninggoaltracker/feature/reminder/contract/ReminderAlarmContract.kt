package com.jeong.runninggoaltracker.feature.reminder.contract

internal object ReminderAlarmContract {
    const val ACTION_REMINDER_ALARM_FORMAT =
        "com.jeong.runninggoaltracker.REMINDER_ALARM_%1\$d_%2\$d"
    const val EXTRA_ID = "id"
    const val EXTRA_HOUR = "hour"
    const val EXTRA_MINUTE = "minute"
    const val EXTRA_DAY_OF_WEEK = "dayOfWeek"
}
