package com.jeong.runninggoaltracker.feature.goal.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoalScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shows_current_goal_when_available() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                GoalScreen(
                    state = GoalUiState(
                        currentGoalKm = 15.0
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
    fun shows_no_current_goal_message_when_goal_not_set() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                GoalScreen(
                    state = GoalUiState(
                        currentGoalKm = null
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
                        weeklyGoalInput = "10"
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
    fun updates_goal_input_via_callback() {
        val state = MutableStateFlow(GoalUiState(weeklyGoalInput = ""))
        composeRule.setContent {
            val uiState by state.collectAsState()
            RunningGoalTrackerTheme {
                GoalScreen(
                    state = uiState,
                    onGoalChange = { newValue -> state.update { it.copy(weeklyGoalInput = newValue) } },
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithTag("goal_input").performTextInput("7.5")

        assertEquals("7.5", state.value.weeklyGoalInput)
    }

    @Test
    fun shows_and_hides_error_messages() {
        val state = MutableStateFlow(GoalUiState())

        composeRule.setContent {
            RunningGoalTrackerTheme {
                val uiState by state.collectAsState()
                GoalScreen(
                    state = uiState,
                    onGoalChange = {},
                    onSave = {}
                )
            }
        }

        composeRule.onNodeWithText("숫자를 입력해주세요.").assertDoesNotExist()
        composeRule.onNodeWithText("0보다 큰 값을 입력해주세요.").assertDoesNotExist()

        state.value = GoalUiState(error = GoalInputError.INVALID_NUMBER)
        composeRule.onNodeWithText("숫자를 입력해주세요.").assertIsDisplayed()
        composeRule.onNodeWithText("0보다 큰 값을 입력해주세요.").assertDoesNotExist()

        state.value = GoalUiState(error = GoalInputError.NON_POSITIVE)
        composeRule.onNodeWithText("0보다 큰 값을 입력해주세요.").assertIsDisplayed()
        composeRule.onNodeWithText("숫자를 입력해주세요.").assertDoesNotExist()

        state.value = GoalUiState(error = null)
        composeRule.onNodeWithText("숫자를 입력해주세요.").assertDoesNotExist()
        composeRule.onNodeWithText("0보다 큰 값을 입력해주세요.").assertDoesNotExist()
    }
}
