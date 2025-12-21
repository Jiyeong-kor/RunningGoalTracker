package com.jeong.runninggoaltracker

import android.os.Build
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.jeong.runninggoaltracker.feature.home.presentation.ActivityLogUiModel
import com.jeong.runninggoaltracker.feature.home.presentation.ActivityRecognitionUiState
import com.jeong.runninggoaltracker.feature.home.presentation.HomeScreen
import com.jeong.runninggoaltracker.feature.home.presentation.HomeUiState
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import com.jeong.runninggoaltracker.shared.designsystem.util.toDistanceLabel
import java.time.LocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
class HomeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun shows_weekly_summary_and_recent_activity_logs() {
        val now = LocalDateTime.of(2024, 11, 8, 9, 30)
        val logs = listOf(
            ActivityLogUiModel(time = now.minusDays(1).toString(), label = "STILL"),
            ActivityLogUiModel(time = now.toString(), label = "RUNNING")
        )
        val weeklyGoalKm = 20.0
        val totalThisWeekKm = 12.5

        composeRule.setContent {
            RunningGoalTrackerTheme {
                HomeScreen(
                    uiState = HomeUiState(
                        weeklyGoalKm = weeklyGoalKm,
                        totalThisWeekKm = totalThisWeekKm,
                        recordCountThisWeek = 3,
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

        composeRule.onNodeWithTag("currentActivityText").assertIsDisplayed()
        composeRule.onNodeWithText("주간 목표: ${weeklyGoalKm.toDistanceLabel()}").assertIsDisplayed()
        composeRule.onNodeWithText("이번 주 누적 거리: ${totalThisWeekKm.toDistanceLabel()}").assertIsDisplayed()
        composeRule.onNodeWithText("이번 주 러닝 횟수: 3 회").assertIsDisplayed()
        composeRule.onNodeWithText("50% 달성").assertIsDisplayed()
        composeRule.onNodeWithText("최근 활동 로그").assertIsDisplayed()
        composeRule.onNodeWithText("STILL").assertIsDisplayed()
        composeRule.onAllNodesWithText("RUNNING")[1].assertIsDisplayed()
    }
}
