package com.jeong.runninggoaltracker.feature.goal.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class GoalScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shows_current_goal_when_available() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                GoalScreen(
                    state = GoalUiState(
                        currentGoalKm = 15.0,
                        weeklyGoalInput = "",
                        error = null
                    ),
                    onGoalChange = {},
                    onSave = {}
                )
            }
        }

        composeRule
            .onNodeWithText("현재 목표: 15.0 km")
            .assertIsDisplayed()
    }

    @Test
    fun shows_error_message_for_invalid_input() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                GoalScreen(
                    state = GoalUiState(
                        currentGoalKm = null,
                        weeklyGoalInput = "abc",
                        error = GoalInputError.INVALID_NUMBER
                    ),
                    onGoalChange = {},
                    onSave = {}
                )
            }
        }

        composeRule
            .onNodeWithText("숫자를 입력해주세요.")
            .assertIsDisplayed()
    }

    @Test
    fun triggers_save_action_when_button_clicked() {
        var saveInvoked = false

        composeRule.setContent {
            RunningGoalTrackerTheme {
                GoalScreen(
                    state = GoalUiState(
                        currentGoalKm = 8.0,
                        weeklyGoalInput = "10",
                        error = null
                    ),
                    onGoalChange = {},
                    onSave = { saveInvoked = true }
                )
            }
        }

        composeRule.onNodeWithText("저장하기").performClick()

        assertTrue(saveInvoked)
    }
}
