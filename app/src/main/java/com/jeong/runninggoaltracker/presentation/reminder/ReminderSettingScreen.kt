package com.jeong.runninggoaltracker.presentation.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.R
import com.jeong.runninggoaltracker.presentation.common.AppContentCard
import java.util.Calendar

@SuppressLint("ScheduleExactAlarm")
@Composable
fun ReminderSettingScreen(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scheduler = remember { ReminderAlarmScheduler(context) }

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
                horizontal = dimensionResource(R.dimen.padding_screen_horizontal),
                vertical = dimensionResource(R.dimen.padding_screen_vertical)
            ),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.spacing_screen_elements)
        )
    ) {

        val list = state.reminders.filter { it.id != null }

        items(
            count = list.size,
            key = { index -> list[index].id!! }
        ) { index ->
            ReminderCard(
                reminder = list[index],
                viewModel = viewModel,
                scheduler = scheduler,
                context = context,
                colorScheme = colorScheme,
                typography = typography
            )
        }

        item {
            Button(
                onClick = { viewModel.addReminder() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reminder_add_button_label))
            }
        }
    }
}

@SuppressLint("ScheduleExactAlarm")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: ReminderUiState,
    viewModel: ReminderViewModel,
    scheduler: ReminderAlarmScheduler,
    context: Context,
    colorScheme: ColorScheme,
    typography: androidx.compose.material3.Typography,
) {
    val daysOfWeek = rememberDaysOfWeek()

    val showTimePicker = remember { mutableStateOf(false) }

    val displayTimeLabel = run {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
        }
        DateFormat.getTimeFormat(context).format(cal.time)
    }

    val id = reminder.id ?: return

    AppContentCard(
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.card_spacing_medium)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { showTimePicker.value = true },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(
                        R.string.reminder_select_time_format,
                        displayTimeLabel
                    ), style = typography.bodyLarge
                )
            }

            Spacer(
                modifier = Modifier.width(
                    dimensionResource(R.dimen.card_spacing_medium)
                )
            )

            Switch(
                checked = reminder.enabled,
                onCheckedChange = { enabled ->

                    if (enabled && reminder.days.isEmpty()) {
                        Toast.makeText(
                            context,
                            context.getString(
                                R.string.reminder_error_select_at_least_one_day
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Switch
                    }

                    viewModel.updateEnabled(id, enabled)

                    if (enabled && reminder.days.isNotEmpty()) {
                        scheduler.schedule(id, reminder.hour, reminder.minute, reminder.days)
                    } else {
                        scheduler.cancel(id, reminder.hour, reminder.minute, reminder.days)
                    }
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
                dimensionResource(R.dimen.card_spacing_extra_small)
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
                            viewModel.toggleDay(id, dayInt)

                            if (reminder.enabled) {
                                scheduler.cancel(
                                    id,
                                    reminder.hour,
                                    reminder.minute,
                                    reminder.days
                                )

                                val newDays = if (reminder.days.contains(dayInt)) {
                                    reminder.days.minus(dayInt)
                                } else {
                                    reminder.days.plus(dayInt)
                                }
                                if (newDays.isNotEmpty()) {
                                    scheduler.schedule(
                                        id,
                                        reminder.hour,
                                        reminder.minute,
                                        newDays
                                    )
                                } else {
                                    viewModel.updateEnabled(id, false)
                                }
                            }
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
                    scheduler.cancel(id, reminder.hour, reminder.minute, reminder.days)
                    viewModel.deleteReminder(id)
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
        TimePickerDialog(
            onDismissRequest = { showTimePicker.value = false },
            confirmButton = {
                Button(onClick = {
                    val h = timeState.hour
                    val m = timeState.minute

                    viewModel.updateTime(id, h, m)

                    if (reminder.enabled && reminder.days.isNotEmpty()) {
                        scheduler.cancel(id, reminder.hour, reminder.minute, reminder.days)
                        scheduler.schedule(id, h, m, reminder.days)
                    }

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
            title = { Text(stringResource(R.string.reminder_dialog_title_select_time)) }
        ) {
            TimePicker(state = timeState)
        }
    }
}

@Composable
fun DaySelectionButton(
    dayName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (isSelected) colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayName,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
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
