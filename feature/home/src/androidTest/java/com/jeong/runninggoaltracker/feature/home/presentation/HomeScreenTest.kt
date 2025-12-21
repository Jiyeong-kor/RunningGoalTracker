package com.jeong.runninggoaltracker.feature.home.presentation

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import com.jeong.runninggoaltracker.shared.designsystem.util.toDistanceLabel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun shows_permission_message_and_default_summary() {
        composeRule.setContent {
            RunningGoalTrackerTheme {
                HomeScreen(
                    uiState = HomeUiState(),
                    activityState = ActivityRecognitionUiState(label = "NO_PERMISSION"),
                    activityLogs = emptyList(),
                    onRecordClick = {},
                    onGoalClick = {},
                    onReminderClick = {}
                )
            }
        }

        composeRule.onNodeWithText("활동 권한이 필요합니다").assertIsDisplayed()
        composeRule.onNodeWithText("주간 목표: 설정되지 않음").assertIsDisplayed()
        composeRule.onNodeWithText("이번 주 누적 거리: ${0.0.toDistanceLabel()}").assertIsDisplayed()
        composeRule.onNodeWithText("이번 주 러닝 횟수: 0 회").assertIsDisplayed()
    }

    @Test
    fun shows_only_last_five_activity_logs_in_reverse_order() {
        val logs = (0..5).map { index ->
            ActivityLogUiModel(
                time = "2024-06-0${index + 1}",
                label = "Log$index"
            )
        }

        composeRule.setContent {
            RunningGoalTrackerTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        weeklyGoalKm = 15.0,
                        totalThisWeekKm = 7.5,
                        recordCountThisWeek = 2,
                        progress = 0.5f
                    ),
                    activityState = ActivityRecognitionUiState(label = "RUNNING"),
                    activityLogs = logs,
                    onRecordClick = {},
                    onGoalClick = {},
                    onReminderClick = {}
                )
            }
        }

        composeRule.onNodeWithTag("activityLogLabelText_Log5").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("activityLogLabelText_Log1").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("activityLogLabelText_Log0").assertDoesNotExist()
    }
}
