package com.jeong.runninggoaltracker.feature.record.presentation

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
class RecordScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shows_empty_state_when_no_records() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                RecordScreen(
                    uiState = RecordUiState(activityLabel = "UNKNOWN"),
                    onStartActivityRecognition = {},
                    onStopActivityRecognition = {},
                    onPermissionDenied = {},
                    onDistanceChange = {},
                    onDurationChange = {},
                    onSaveRecord = {}
                )
            }
        }

        composeRule
            .onNodeWithText("저장된 기록이 없습니다.")
            .assertIsDisplayed()
    }

    @Test
    fun displays_saved_records_with_formatted_values() {
        val record = RunningRecord(
            date = LocalDate.of(2024, 2, 1),
            distanceKm = 5.0,
            durationMinutes = 30
        )

        composeRule.setContent {
            RunningGoalTrackerTheme {
                RecordScreen(
                    uiState = RecordUiState(
                        records = listOf(record),
                        activityLabel = "RUNNING"
                    ),
                    onStartActivityRecognition = {},
                    onStopActivityRecognition = {},
                    onPermissionDenied = {},
                    onDistanceChange = {},
                    onDurationChange = {},
                    onSaveRecord = {}
                )
            }
        }

        composeRule.onNodeWithText("2월 1일 (목)").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("5 km").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("30 분").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shows_activity_permission_message_when_denied() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                RecordScreen(
                    uiState = RecordUiState(activityLabel = "NO_PERMISSION"),
                    onStartActivityRecognition = {},
                    onStopActivityRecognition = {},
                    onPermissionDenied = {},
                    onDistanceChange = {},
                    onDurationChange = {},
                    onSaveRecord = {}
                )
            }
        }

        composeRule
            .onNodeWithText("현재 활동: 활동 권한이 필요합니다")
            .assertIsDisplayed()
    }

    @Test
    fun shows_error_message_when_activity_request_fails() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                RecordScreen(
                    uiState = RecordUiState(activityLabel = "REQUEST_FAILED"),
                    onStartActivityRecognition = {},
                    onStopActivityRecognition = {},
                    onPermissionDenied = {},
                    onDistanceChange = {},
                    onDurationChange = {},
                    onSaveRecord = {}
                )
            }
        }

        composeRule
            .onNodeWithText("현재 활동: 활동 인식에 실패했습니다")
            .assertIsDisplayed()
    }

    @Test
    fun updates_inputs_and_triggers_callbacks() {
        val state = MutableStateFlow(RecordUiState(activityLabel = "UNKNOWN"))
        var startInvoked = false
        var stopInvoked = false
        var saveInvoked = false

        composeRule.setContent {
            val uiState by state.collectAsState()
            RunningGoalTrackerTheme {
                RecordScreen(
                    uiState = uiState,
                    onStartActivityRecognition = {
                        startInvoked = true
                    },
                    onStopActivityRecognition = { stopInvoked = true },
                    onPermissionDenied = {},
                    onDistanceChange = { newValue -> state.update { it.copy(distanceInput = newValue) } },
                    onDurationChange = { newValue -> state.update { it.copy(durationInput = newValue) } },
                    onSaveRecord = { saveInvoked = true }
                )
            }
        }

        composeRule.onNodeWithTag("distance_input").performScrollTo().performTextInput("10.5")
        composeRule.onNodeWithTag("duration_input").performScrollTo().performTextInput("40")
        composeRule.onNodeWithText("활동 감지 시작").performScrollTo().performClick()
        composeRule.onNodeWithText("활동 감지 중지").performScrollTo().performClick()
        composeRule.onNodeWithText("저장하기").performScrollTo().performClick()

        assertEquals("10.5", state.value.distanceInput)
        assertEquals("40", state.value.durationInput)
        assertTrue(startInvoked)
        assertTrue(stopInvoked)
        assertTrue(saveInvoked)
    }

    @Test
    fun shows_error_messages_for_invalid_inputs() {
        val state = MutableStateFlow(RecordUiState(activityLabel = "UNKNOWN"))

        composeRule.setContent {
            RunningGoalTrackerTheme {
                val uiState by state.collectAsState()
                RecordScreen(
                    uiState = uiState,
                    onStartActivityRecognition = {},
                    onStopActivityRecognition = {},
                    onPermissionDenied = {},
                    onDistanceChange = {},
                    onDurationChange = {},
                    onSaveRecord = {}
                )
            }
        }

        state.value = RecordUiState(error = RecordInputError.INVALID_NUMBER)

        composeRule
            .onNodeWithText("숫자 형식으로 입력해주세요.")
            .assertIsDisplayed()

        state.value = RecordUiState(error = RecordInputError.NON_POSITIVE)

        composeRule
            .onNodeWithText("0보다 큰 값을 입력해주세요.")
            .assertIsDisplayed()
    }
}
