package com.jeong.runninggoaltracker.feature.reminder.presentation

import androidx.annotation.StringRes

interface UserMessageHandler {
    fun showMessage(message: UiMessage)
}

data class UiMessage(
    val text: String? = null,
    @param:StringRes val messageResId: Int? = null
)

interface NotificationPermissionRequester {
    fun requestPermissionIfNeeded()
}

interface ReminderTimeFormatter {
    fun formatTime(hour: Int, minute: Int): String
    fun periodLabel(hour: Int): String
}

interface DaysOfWeekLabelProvider {
    fun labels(): Map<Int, String>
}
