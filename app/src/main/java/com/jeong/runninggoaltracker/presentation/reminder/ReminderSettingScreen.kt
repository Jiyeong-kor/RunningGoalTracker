package com.jeong.runninggoaltracker.presentation.reminder

import android.annotation.SuppressLint
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.R
import java.util.Calendar

@SuppressLint("ScheduleExactAlarm")
@Composable
fun ReminderSettingScreen(
    viewModel: ReminderViewModel = hiltViewModel(),
    onBack: () -> Unit
) {

    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scheduler = remember { ReminderAlarmScheduler(context) }

    var hourText by remember { mutableStateOf(state.hour.toString()) }
    var minuteText by remember { mutableStateOf(state.minute.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    val errorCheckTimeSelection = stringResource(R.string.error_check_time_selection)

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val pendingHour = hourText.toIntOrNull() ?: state.hour
    val pendingMinute = minuteText.toIntOrNull() ?: state.minute

    val displayTimeLabel = run {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, pendingHour)
            set(Calendar.MINUTE, pendingMinute)
        }
        DateFormat.getTimeFormat(context).format(cal.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.reminder_select_time_format,
                        displayTimeLabel
                    ),
                    style = typography.bodyLarge
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(
                                R.string.reminder_select_time_format,
                                displayTimeLabel
                            ), style = typography.bodyLarge
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.reminder_toggle_label),
                        style = typography.bodyLarge
                    )
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = { enabled ->
                            viewModel.updateEnabled(enabled)
                        }
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = colorScheme.error,
                        style = typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        val h = hourText.toIntOrNull()
                        val m = minuteText.toIntOrNull()

                        if (h != null && m != null) {
                            viewModel.updateTime(h, m)

                            if (state.enabled) {
                                scheduler.schedule(h, m)
                            } else {
                                scheduler.cancel(h, m)
                            }
                            onBack()
                        } else {
                            errorText = errorCheckTimeSelection
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.button_save))
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        if (showTimePicker) {
            val timeState = rememberTimePickerState(
                initialHour = hourText.toIntOrNull() ?: state.hour,
                initialMinute = minuteText.toIntOrNull() ?: state.minute
            )
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },

                confirmButton = {
                    Button(onClick = {
                        hourText = timeState.hour.toString()
                        minuteText = timeState.minute.toString()
                        errorText = null
                        showTimePicker = false
                    }) {
                        Text(stringResource(R.string.button_confirm))
                    }
                },

                dismissButton = {
                    OutlinedButton(onClick = { showTimePicker = false }) {
                        Text(stringResource(R.string.button_cancel))
                    }
                },

                title = { Text(stringResource(R.string.reminder_dialog_title_select_time)) }) {
                TimePicker(state = timeState)
            }
        }
    }
}
