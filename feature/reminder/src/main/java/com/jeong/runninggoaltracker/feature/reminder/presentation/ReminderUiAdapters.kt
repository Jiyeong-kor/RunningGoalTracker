package com.jeong.runninggoaltracker.feature.reminder.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.jeong.runninggoaltracker.domain.contract.DateTimeContract
import com.jeong.runninggoaltracker.feature.reminder.R
import com.jeong.runninggoaltracker.feature.reminder.contract.ReminderTimeContract

private class ToastUserMessageHandler(
    private val context: Context
) : UserMessageHandler {
    override fun showMessage(message: UiMessage) {
        val messageResId = message.messageResId ?: return
        Toast.makeText(context, context.getString(messageResId), Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun rememberUserMessageHandler(): UserMessageHandler {
    val context = LocalContext.current
    return remember(context) { ToastUserMessageHandler(context) }
}

private class ResourceReminderTimeFormatter(
    private val amLabel: String,
    private val pmLabel: String,
    private val timeFormat: String
) : ReminderTimeFormatter {
    override fun formatTime(hour: Int, minute: Int): String {
        val displayHour =
            if (hour % ReminderTimeContract.HOUR_CYCLE == ReminderTimeContract.HOUR_REMAINDER_ZERO) {
                ReminderTimeContract.HOUR_CYCLE
            } else {
                hour % ReminderTimeContract.HOUR_CYCLE
            }
        return String.format(timeFormat, displayHour, minute)
    }

    override fun periodLabel(hour: Int): String =
        if (hour < ReminderTimeContract.HOUR_CYCLE) amLabel else pmLabel
}

@Composable
fun rememberReminderTimeFormatter(): ReminderTimeFormatter {
    val amLabel = stringResource(R.string.reminder_time_period_am)
    val pmLabel = stringResource(R.string.reminder_time_period_pm)
    val timeFormat = stringResource(R.string.reminder_time_format)
    return remember(amLabel, pmLabel, timeFormat) {
        ResourceReminderTimeFormatter(amLabel, pmLabel, timeFormat)
    }
}

private class ResourceDaysOfWeekLabelProvider(
    private val labels: Map<Int, String>
) : DaysOfWeekLabelProvider {
    override fun labels(): Map<Int, String> = labels
}

@Composable
fun rememberDaysOfWeekLabelProvider(): DaysOfWeekLabelProvider {
    val labels = mapOf(
        DateTimeContract.WEEK_START_DAY to stringResource(R.string.day_sun),
        java.util.Calendar.MONDAY to stringResource(R.string.day_mon),
        java.util.Calendar.TUESDAY to stringResource(R.string.day_tue),
        java.util.Calendar.WEDNESDAY to stringResource(R.string.day_wed),
        java.util.Calendar.THURSDAY to stringResource(R.string.day_thu),
        java.util.Calendar.FRIDAY to stringResource(R.string.day_fri),
        java.util.Calendar.SATURDAY to stringResource(R.string.day_sat)
    )
    return remember(labels) { ResourceDaysOfWeekLabelProvider(labels) }
}
