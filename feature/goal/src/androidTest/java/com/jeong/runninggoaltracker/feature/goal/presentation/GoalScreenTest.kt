package com.jeong.runninggoaltracker.feature.goal.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import org.junit.Assert.assertEquals
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
    fun shows_no_current_goal_message_when_goal_not_set() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                GoalScreen(
                    state = GoalUiState(
                        currentGoalKm = null,
                        weeklyGoalInput = "",
                        error = null
                    ),
                    onGoalChange = {},
                    onSave = {}
                )
            }
        }

        composeRule
            .onNodeWithText("현재 설정된 목표가 없습니다.")
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

    @Test
    fun updates_goal_input_and_shows_non_positive_error() {
        var latestGoalInput = ""

        composeRule.setContent {
            RunningGoalTrackerTheme {
                GoalScreen(
                    state = GoalUiState(
                        currentGoalKm = 5.0,
                        weeklyGoalInput = "0",
                        error = GoalInputError.NON_POSITIVE
                    ),
                    onGoalChange = { latestGoalInput = it },
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("주간 목표 거리 (km)").performTextClearance()
        composeRule.onNodeWithText("주간 목표 거리 (km)").performTextInput("7.5")

        composeRule
            .onNodeWithText("0보다 큰 값을 입력해주세요.")
            .assertIsDisplayed()
        assertEquals("7.5", latestGoalInput)
    }
}
