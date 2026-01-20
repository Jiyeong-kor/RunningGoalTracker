package com.jeong.runninggoaltracker.feature.reminder.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.annotation.IntegerRes
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.reminder.R
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.extension.throttleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.appAccentColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appBackgroundColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSurfaceColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextMutedColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextPrimaryColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import java.util.Calendar

@Composable
fun ReminderRoute(
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val userMessageHandler = rememberUserMessageHandler()
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
        daysOfWeekLabelProvider = daysOfWeekLabelProvider
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
    daysOfWeekLabelProvider: DaysOfWeekLabelProvider
) {
    val onAddReminderThrottled = rememberThrottleClick(onClick = onAddReminder)
    val accentColor = appAccentColor()
    val backgroundColor = appBackgroundColor()
    val textPrimary = appTextPrimaryColor()
    val paddingHorizontal = dimensionResource(id = R.dimen.reminder_padding_horizontal)
    val spacingMd = dimensionResource(id = R.dimen.reminder_spacing_md)
    val spacingLg = dimensionResource(id = R.dimen.reminder_spacing_lg)
    val iconButtonSize = dimensionResource(id = R.dimen.reminder_icon_button_size)
    val iconButtonCornerRadius = dimensionResource(id = R.dimen.reminder_icon_button_corner_radius)
    val iconSize = dimensionResource(id = R.dimen.reminder_icon_size)
    val titleTextSize = dimensionResource(id = R.dimen.reminder_text_title_size).value.sp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = paddingHorizontal)
    ) {
        Spacer(modifier = Modifier.height(spacingMd))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.reminder_title_settings),
                color = textPrimary,
                fontSize = titleTextSize,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = onAddReminderThrottled,
                modifier = Modifier
                    .background(accentColor, RoundedCornerShape(iconButtonCornerRadius))
                    .size(iconButtonSize)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacingLg))

        LazyColumn(
            contentPadding = PaddingValues(bottom = spacingLg),
            verticalArrangement = Arrangement.spacedBy(spacingMd)
        ) {
            val list = state.reminders
            items(count = list.size, key = { index -> list[index].id }) { index ->
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
    val accentColor = appAccentColor()
    val surfaceColor = appSurfaceColor()
    val textMuted = appTextMutedColor()
    val textPrimary = appTextPrimaryColor()
    val showTimePicker = remember { mutableStateOf(false) }
    val id = reminder.id
    val daysOfWeek = daysOfWeekLabelProvider.labels()
    val onDeleteReminderThrottled = rememberThrottleClick(onClick = { onDeleteReminder(id) })
    val cardCornerRadius = dimensionResource(id = R.dimen.reminder_card_corner_radius)
    val cardContentPadding = dimensionResource(id = R.dimen.reminder_card_content_padding)
    val spacingMd = dimensionResource(id = R.dimen.reminder_spacing_md)
    val daySpacing = dimensionResource(id = R.dimen.reminder_spacing_sm)
    val periodTextSize = dimensionResource(id = R.dimen.reminder_text_period_size).value.sp
    val timeTextSize = dimensionResource(id = R.dimen.reminder_text_time_size).value.sp
    val disabledSurfaceAlpha = alphaFromPercent(R.integer.reminder_alpha_disabled_surface_percent)

    AppSurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(cardCornerRadius),
        containerColor = if (reminder.enabled) surfaceColor else Color.White.copy(alpha = disabledSurfaceAlpha),
        contentPadding = PaddingValues(cardContentPadding)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.throttleClick { showTimePicker.value = true }) {
                    Text(
                        timeFormatter.periodLabel(reminder.hour),
                        color = textMuted,
                        fontSize = periodTextSize,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        timeFormatter.formatTime(reminder.hour, reminder.minute),
                        color = textPrimary,
                        fontSize = timeTextSize,
                        fontWeight = FontWeight.Black
                    )
                }
                Switch(
                    checked = reminder.enabled,
                    onCheckedChange = null,
                    modifier = Modifier.throttleClick {
                        val enabled = !reminder.enabled
                        if (enabled && reminder.days.isEmpty()) {
                            messageHandler.showMessage(
                                UiMessage(messageResId = R.string.reminder_error_select_at_least_one_day)
                            )
                        } else {
                            onToggleReminder(id, enabled)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = accentColor,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(spacingMd))

            Row(horizontalArrangement = Arrangement.spacedBy(daySpacing)) {
                daysOfWeek.forEach { (dayInt, dayName) ->
                    val isSelected = reminder.days.contains(dayInt)
                    DayBubble(dayName, isSelected) {
                        onToggleDay(id, dayInt)
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onDeleteReminderThrottled) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
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
private fun DayBubble(day: String, isSelected: Boolean, onClick: () -> Unit) {
    val accentColor = appAccentColor()
    val bubbleSize = dimensionResource(id = R.dimen.reminder_day_bubble_size)
    val bubbleCornerRadius = dimensionResource(id = R.dimen.reminder_day_bubble_corner_radius)
    val dayTextSize = dimensionResource(id = R.dimen.reminder_text_day_size).value.sp
    val selectedAlpha = alphaFromPercent(R.integer.reminder_alpha_selected_day_background_percent)
    val unselectedTextAlpha = alphaFromPercent(R.integer.reminder_alpha_unselected_day_text_percent)

    Box(
        modifier = Modifier
            .size(bubbleSize)
            .background(
                color = if (isSelected) accentColor.copy(alpha = selectedAlpha) else Color.Transparent,
                shape = RoundedCornerShape(bubbleCornerRadius)
            )
            .throttleClick { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            day,
            color = if (isSelected) accentColor else Color.Gray.copy(alpha = unselectedTextAlpha),
            fontSize = dayTextSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun alphaFromPercent(@IntegerRes percentResId: Int): Float =
    integerResource(id = percentResId).toFloat() /
            integerResource(id = R.integer.reminder_percent_divisor).toFloat()

@Preview(showBackground = true)
@Composable
private fun ReminderScreenPreview() {
    val state = ReminderListUiState(
        reminders = listOf(
            ReminderUiState(
                id = 1,
                hour = 6,
                minute = 30,
                enabled = true,
                days = setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
            ),
            ReminderUiState(
                id = 2,
                hour = 20,
                minute = 0,
                enabled = false,
                days = emptySet()
            )
        )
    )
    val messageHandler = remember {
        object : UserMessageHandler {
            override fun showMessage(message: UiMessage) = Unit
        }
    }
    val timeFormatter = rememberReminderTimeFormatter()
    val daysOfWeekLabelProvider = rememberDaysOfWeekLabelProvider()

    RunningGoalTrackerTheme {
        ReminderScreen(
            state = state,
            onAddReminder = {},
            onDeleteReminder = {},
            onToggleReminder = { _, _ -> },
            onUpdateTime = { _, _, _ -> },
            onToggleDay = { _, _ -> },
            messageHandler = messageHandler,
            timeFormatter = timeFormatter,
            daysOfWeekLabelProvider = daysOfWeekLabelProvider
        )
    }
}
