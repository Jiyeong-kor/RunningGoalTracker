package com.jeong.runninggoaltracker.feature.reminder.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.jeong.runninggoaltracker.feature.reminder.R
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReminderScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shows_reminders_and_invokes_actions() {
        var addCalled = false
        var toggleCalled = false
        var toggledDay: Pair<Int, Int>? = null
        var deletedId: Int? = null

        val reminder = ReminderUiState(
            id = 1,
            hour = 6,
            minute = 30,
            enabled = false,
            days = setOf(java.util.Calendar.MONDAY)
        )

        val context = InstrumentationRegistry.getInstrumentation().targetContext

        composeRule.setContent {
            RunningGoalTrackerTheme {
                val messageHandler = rememberUserMessageHandler()
                val timeFormatter = rememberReminderTimeFormatter()
                val daysOfWeekLabelProvider = rememberDaysOfWeekLabelProvider()
                ReminderScreen(
                    state = ReminderListUiState(reminders = listOf(reminder)),
                    onAddReminder = { addCalled = true },
                    onDeleteReminder = { deletedId = it },
                    onToggleReminder = { _, enabled -> toggleCalled = enabled },
                    onUpdateTime = { _, _, _ -> },
                    onToggleDay = { id, day -> toggledDay = id to day },
                    messageHandler = messageHandler,
                    timeFormatter = timeFormatter,
                    daysOfWeekLabelProvider = daysOfWeekLabelProvider
                )
            }
        }

        val reminderCountText = context.getString(R.string.reminder_total_count, 1)
        val addReminderLabel = context.getString(R.string.reminder_add_button_label)
        val mondayLabel = context.getString(R.string.day_mon)
        val deleteLabel = context.getString(R.string.reminder_delete_button_label)

        composeRule.onNodeWithText(reminderCountText).assertIsDisplayed()
        composeRule.onNodeWithText(addReminderLabel).performClick()
        composeRule.onAllNodes(isToggleable()).onFirst().performClick()
        composeRule.onNodeWithText(mondayLabel).performClick()
        composeRule.onNodeWithText(deleteLabel).performClick()

        assertTrue(addCalled)
        assertTrue(toggleCalled)
        assertEquals(1 to java.util.Calendar.MONDAY, toggledDay)
        assertEquals(1, deletedId)
    }
}
