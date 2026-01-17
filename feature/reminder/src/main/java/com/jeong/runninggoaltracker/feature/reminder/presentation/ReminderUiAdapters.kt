package com.jeong.runninggoaltracker.feature.reminder.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.jeong.runninggoaltracker.feature.reminder.R

private class ToastUserMessageHandler(
    private val context: Context
) : UserMessageHandler {
    override fun showMessage(message: UiMessage) {
        val textToShow = message.text ?: message.messageResId?.let(context::getString) ?: return
        Toast.makeText(context, textToShow, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun rememberUserMessageHandler(): UserMessageHandler {
    val context = LocalContext.current
    return remember(context) { ToastUserMessageHandler(context) }
}

private class AndroidNotificationPermissionRequester(
    private val context: Context,
    private val permissionLauncher: ActivityResultLauncher<String>
) : NotificationPermissionRequester {
    override fun requestPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun rememberNotificationPermissionRequester(
    onPermissionDenied: () -> Unit
): NotificationPermissionRequester {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            onPermissionDenied()
        }
    }

    return remember(context, permissionLauncher) {
        AndroidNotificationPermissionRequester(context, permissionLauncher)
    }
}

private class ResourceReminderTimeFormatter(
    private val amLabel: String,
    private val pmLabel: String,
    private val timeFormat: String
) : ReminderTimeFormatter {
    override fun formatTime(hour: Int, minute: Int): String {
        val displayHour = if (hour % 12 == 0) 12 else hour % 12
        return String.format(timeFormat, displayHour, minute)
    }

    override fun periodLabel(hour: Int): String {
        return if (hour < 12) amLabel else pmLabel
    }
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
        java.util.Calendar.SUNDAY to stringResource(R.string.day_sun),
        java.util.Calendar.MONDAY to stringResource(R.string.day_mon),
        java.util.Calendar.TUESDAY to stringResource(R.string.day_tue),
        java.util.Calendar.WEDNESDAY to stringResource(R.string.day_wed),
        java.util.Calendar.THURSDAY to stringResource(R.string.day_thu),
        java.util.Calendar.FRIDAY to stringResource(R.string.day_fri),
        java.util.Calendar.SATURDAY to stringResource(R.string.day_sat)
    )
    return remember(labels) { ResourceDaysOfWeekLabelProvider(labels) }
}
