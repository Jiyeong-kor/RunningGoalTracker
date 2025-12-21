package com.jeong.runninggoaltracker.feature.record.presentation

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import java.time.LocalDate
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

        composeRule.onNodeWithText("2월 1일 (목)").assertIsDisplayed()
        composeRule.onNodeWithText("5 km").assertIsDisplayed()
        composeRule.onNodeWithText("30 분").assertIsDisplayed()
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
    fun updates_inputs_and_triggers_callbacks() {
        var distanceInput = ""
        var durationInput = ""
        var startInvoked = false
        var stopInvoked = false
        var saveInvoked = false

        composeRule.setContent {
            RunningGoalTrackerTheme {
                RecordScreen(
                    uiState = RecordUiState(activityLabel = "UNKNOWN"),
                    onStartActivityRecognition = {
                        startInvoked = true
                    },
                    onStopActivityRecognition = { stopInvoked = true },
                    onPermissionDenied = {},
                    onDistanceChange = { distanceInput = it },
                    onDurationChange = { durationInput = it },
                    onSaveRecord = { saveInvoked = true }
                )
            }
        }

        composeRule.onNodeWithText("거리 (km)").performTextInput("10.5")
        composeRule.onNodeWithText("시간 (분)").performTextInput("40")
        composeRule.onNodeWithText("활동 감지 시작").performClick()
        composeRule.onNodeWithText("활동 감지 중지").performClick()
        composeRule.onNodeWithText("저장하기").performClick()

        assertEquals("10.5", distanceInput)
        assertEquals("40", durationInput)
        assertTrue(startInvoked)
        assertTrue(stopInvoked)
        assertTrue(saveInvoked)
    }
}
