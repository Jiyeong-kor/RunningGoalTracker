package com.jeong.runninggoaltracker.feature.reminder.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.reminder.R
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.shared.designsystem.common.DaySelectionButton
import java.util.Calendar
import com.jeong.runninggoaltracker.shared.designsystem.R as SharedR

@Composable
fun ReminderRoute(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    ReminderScreen(
        state = state,
        context = context,
        onAddReminder = viewModel::addReminder,
        onDeleteReminder = viewModel::deleteReminder,
        onToggleReminder = viewModel::updateEnabled,
        onUpdateTime = viewModel::updateTime,
        onToggleDay = viewModel::toggleDay
    )
}

@SuppressLint("ScheduleExactAlarm")
@Composable
fun ReminderScreen(
    state: ReminderListUiState,
    context: Context,
    onAddReminder: () -> Unit,
    onDeleteReminder: (Int) -> Unit,
    onToggleReminder: (Int, Boolean) -> Unit,
    onUpdateTime: (Int, Int, Int) -> Unit,
    onToggleDay: (Int, Int) -> Unit
) {
    val reminderErrorNotificationPermissionDenied =
        stringResource(R.string.reminder_error_notification_permission_denied)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(
                context,
                reminderErrorNotificationPermissionDenied,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = dimensionResource(SharedR.dimen.padding_screen_horizontal),
                vertical = dimensionResource(SharedR.dimen.padding_screen_vertical)
            ),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(SharedR.dimen.spacing_screen_elements)
        )
    ) {

        val list = state.reminders.filter { it.id != null }

        items(
            count = list.size,
            key = { index -> list[index].id!! }
        ) { index ->
            ReminderCard(
                reminder = list[index],
                onToggleReminder = onToggleReminder,
                onUpdateTime = onUpdateTime,
                onToggleDay = onToggleDay,
                onDeleteReminder = onDeleteReminder,
                context = context,
                colorScheme = colorScheme,
                typography = typography
            )
        }

        item {
            Button(
                onClick = { onAddReminder() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reminder_add_button_label))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ReminderCard(
    reminder: ReminderUiState,
    onToggleReminder: (Int, Boolean) -> Unit,
    onUpdateTime: (Int, Int, Int) -> Unit,
    onToggleDay: (Int, Int) -> Unit,
    onDeleteReminder: (Int) -> Unit,
    context: Context,
    colorScheme: ColorScheme,
    typography: androidx.compose.material3.Typography
) {
    val showTimePicker = remember { mutableStateOf(false) }
    val id = reminder.id ?: return

    val daysOfWeek = rememberDaysOfWeek()

    AppContentCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    R.string.reminder_select_time_format,
                    formatTime(reminder.hour, reminder.minute)
                ),
                style = typography.titleMedium,
                modifier = Modifier.clickable { showTimePicker.value = true }
            )
            Switch(
                checked = reminder.enabled,
                onCheckedChange = { enabled ->
                    if (enabled && reminder.days.isEmpty()) {
                        Toast.makeText(
                            context,
                            R.string.reminder_error_select_at_least_one_day,
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Switch
                    }

                    onToggleReminder(id, enabled)
                }
            )
        }

        Text(
            text = stringResource(R.string.reminder_select_days_label),
            style = typography.bodyMedium
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(SharedR.dimen.card_spacing_extra_small)
            )
        ) {
            daysOfWeek.forEach { (dayInt, dayName) ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    DaySelectionButton(
                        dayName = dayName,
                        isSelected = reminder.days.contains(dayInt),
                        onClick = {
                            onToggleDay(id, dayInt)
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    onDeleteReminder(id)
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colorScheme.error)
            ) {
                Text(stringResource(R.string.reminder_delete_button_label))
            }
        }
    }

    if (showTimePicker.value) {
        val timeState = rememberTimePickerState(
            initialHour = reminder.hour,
            initialMinute = reminder.minute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker.value = false },
            confirmButton = {
                Button(onClick = {
                    val h = timeState.hour
                    val m = timeState.minute

                    onUpdateTime(id, h, m)

                    showTimePicker.value = false
                }) {
                    Text(stringResource(R.string.button_confirm))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTimePicker.value = false }) {
                    Text(stringResource(R.string.button_cancel))
                }
            },
            title = { Text(stringResource(R.string.reminder_dialog_title_select_time)) },
            text = { TimeInput(state = timeState) }
        )
    }
}

@Composable
private fun rememberDaysOfWeek(): Map<Int, String> {
    return mapOf(
        Calendar.SUNDAY to stringResource(R.string.day_sun),
        Calendar.MONDAY to stringResource(R.string.day_mon),
        Calendar.TUESDAY to stringResource(R.string.day_tue),
        Calendar.WEDNESDAY to stringResource(R.string.day_wed),
        Calendar.THURSDAY to stringResource(R.string.day_thu),
        Calendar.FRIDAY to stringResource(R.string.day_fri),
        Calendar.SATURDAY to stringResource(R.string.day_sat)
    )
}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return DateFormat.format("HH:mm", calendar).toString()
}
