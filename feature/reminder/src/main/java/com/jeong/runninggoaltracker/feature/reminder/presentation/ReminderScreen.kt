package com.jeong.runninggoaltracker.feature.reminder.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.reminder.R
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.shared.designsystem.common.DaySelectionButton
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.extension.throttleClick

@Composable
fun ReminderRoute(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val userMessageHandler = rememberUserMessageHandler()
    val notificationPermissionRequester = rememberNotificationPermissionRequester {
        userMessageHandler.showMessage(
            UiMessage(messageResId = R.string.reminder_error_notification_permission_denied)
        )
    }
    val timeFormatter = rememberReminderTimeFormatter()
    val daysOfWeekLabelProvider = rememberDaysOfWeekLabelProvider()

    ReminderScreen(
        state = state,
        onAddReminder = viewModel::addReminder,
        onDeleteReminder = viewModel::deleteReminder,
        onToggleReminder = viewModel::updateEnabled,
        onUpdateTime = viewModel::updateTime,
        onToggleDay = viewModel::toggleDay,
        messageHandler = userMessageHandler,
        timeFormatter = timeFormatter,
        daysOfWeekLabelProvider = daysOfWeekLabelProvider,
        notificationPermissionRequester = notificationPermissionRequester
    )
}

@Composable
fun ReminderScreen(
    state: ReminderListUiState,
    onAddReminder: () -> Unit,
    onDeleteReminder: (Int) -> Unit,
    onToggleReminder: (Int, Boolean) -> Unit,
    onUpdateTime: (Int, Int, Int) -> Unit,
    onToggleDay: (Int, Int) -> Unit,
    messageHandler: UserMessageHandler,
    timeFormatter: ReminderTimeFormatter,
    daysOfWeekLabelProvider: DaysOfWeekLabelProvider,
    notificationPermissionRequester: NotificationPermissionRequester
) {
    val onAddReminderThrottled = rememberThrottleClick(onClick = onAddReminder)

    LaunchedEffect(Unit) {
        notificationPermissionRequester.requestPermissionIfNeeded()
    }

    Scaffold(
        topBar = {
            ReminderTopBar(reminderCount = state.reminders.size)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddReminderThrottled,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    Icons.Filled.Add, contentDescription = null,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val list = state.reminders.filter { it.id != null }
            items(count = list.size, key = { index -> list[index].id!! }) { index ->
                ReminderCard(
                    reminder = list[index],
                    onToggleReminder = onToggleReminder,
                    onUpdateTime = onUpdateTime,
                    onToggleDay = onToggleDay,
                    onDeleteReminder = onDeleteReminder,
                    messageHandler = messageHandler,
                    timeFormatter = timeFormatter,
                    daysOfWeekLabelProvider = daysOfWeekLabelProvider
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderCard(
    reminder: ReminderUiState,
    onToggleReminder: (Int, Boolean) -> Unit,
    onUpdateTime: (Int, Int, Int) -> Unit,
    onToggleDay: (Int, Int) -> Unit,
    onDeleteReminder: (Int) -> Unit,
    messageHandler: UserMessageHandler,
    timeFormatter: ReminderTimeFormatter,
    daysOfWeekLabelProvider: DaysOfWeekLabelProvider
) {
    val showTimePicker = remember { mutableStateOf(false) }
    val id = reminder.id ?: return
    val daysOfWeek = daysOfWeekLabelProvider.labels()
    val onDeleteReminderThrottled = rememberThrottleClick(onClick = { onDeleteReminder(id) })

    AppContentCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.throttleClick { showTimePicker.value = true }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (reminder.enabled) Icons.Rounded.Notifications
                            else Icons.Rounded.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (reminder.enabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        ReminderPeriodText(hour = reminder.hour, timeFormatter = timeFormatter)
                    }
                    Text(
                        text = timeFormatter.formatTime(reminder.hour, reminder.minute),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-1).sp
                        ),
                        color = if (reminder.enabled) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.outline
                    )
                }

                Switch(
                    checked = reminder.enabled,
                    onCheckedChange = { enabled ->
                        if (enabled && reminder.days.isEmpty()) {
                            messageHandler.showMessage(
                                UiMessage(messageResId = R.string.reminder_error_select_at_least_one_day)
                            )
                            return@Switch
                        }
                        onToggleReminder(id, enabled)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeek.forEach { (dayInt, dayName) ->
                    val isSelected = reminder.days.contains(dayInt)
                    DaySelectionButton(
                        dayName = dayName,
                        isSelected = isSelected,
                        modifier = Modifier.size(40.dp),
                        onClick = { onToggleDay(id, dayInt) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        intervalMillis = 500L
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDeleteReminderThrottled,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.reminder_delete_button_label),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showTimePicker.value) {
        val timeState =
            rememberTimePickerState(initialHour = reminder.hour, initialMinute = reminder.minute)
        val onConfirmClick = rememberThrottleClick {
            onUpdateTime(id, timeState.hour, timeState.minute)
            showTimePicker.value = false
        }
        val onDismissClick = rememberThrottleClick {
            showTimePicker.value = false
        }
        AlertDialog(
            onDismissRequest = onDismissClick,
            confirmButton = {
                TextButton(onClick = onConfirmClick) { Text(stringResource(R.string.button_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onDismissClick) { Text(stringResource(R.string.button_cancel)) }
            },
            title = { Text(stringResource(R.string.reminder_add_button_label)) },
            text = { TimeInput(state = timeState) }
        )
    }
}

@Composable
private fun ReminderTopBar(reminderCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(
            text = stringResource(R.string.reminder_total_count, reminderCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ReminderPeriodText(hour: Int, timeFormatter: ReminderTimeFormatter) {
    Text(
        text = timeFormatter.periodLabel(hour),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.outline
    )
}
