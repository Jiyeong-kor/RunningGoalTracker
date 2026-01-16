package com.jeong.runninggoaltracker.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.domain.util.DateFormatter
import com.jeong.runninggoaltracker.shared.designsystem.common.AppProgressBar
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.appAccentColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appBackgroundColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingMd
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingSm
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSurfaceColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextMutedColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextPrimaryColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import kotlinx.coroutines.flow.Flow
import androidx.compose.ui.res.stringResource
import com.jeong.runninggoaltracker.feature.home.R

data class ActivityRecognitionUiState(
    val label: String = "UNKNOWN"
)

data class ActivityLogUiModel(
    val time: Long,
    val label: String
)

@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    activityStateFlow: Flow<ActivityRecognitionUiState>,
    activityLogsFlow: Flow<List<ActivityLogUiModel>>,
    onRecordClick: () -> Unit,
    onGoalClick: () -> Unit,
    onReminderClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val activityState by activityStateFlow.collectAsState(initial = ActivityRecognitionUiState())
    val activityLogs by activityLogsFlow.collectAsState(initial = emptyList())

    HomeScreen(
        uiState = uiState,
        activityState = activityState,
        activityLogs = activityLogs,
        dateFormatter = viewModel.dateFormatter,
        onRecordClick = onRecordClick,
        onGoalClick = onGoalClick,
        onReminderClick = onReminderClick
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    activityState: ActivityRecognitionUiState,
    activityLogs: List<ActivityLogUiModel>,
    dateFormatter: DateFormatter,
    onRecordClick: () -> Unit,
    onGoalClick: () -> Unit,
    onReminderClick: () -> Unit
) {
    val weeklyGoalKm = uiState.weeklyGoalKm ?: 0.0
    val totalThisWeekKm = uiState.totalThisWeekKm
    val remainingKm = (weeklyGoalKm - totalThisWeekKm).coerceAtLeast(0.0).toFloat()
    val progress = uiState.progress
    val accentColor = appAccentColor()
    val backgroundColor = appBackgroundColor()
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()

    val onRecordClickThrottled = rememberThrottleClick(onClick = onRecordClick)
    val onGoalClickThrottled = rememberThrottleClick(onClick = onGoalClick)
    val onReminderClickThrottled = rememberThrottleClick(onClick = onReminderClick)
    var isAnonymousBannerVisible by rememberSaveable { mutableStateOf(true) }

    val recentActivities = activityLogs
        .takeLast(3)
        .reversed()
        .map { log ->
            ActivityItem(
                date = dateFormatter.formatToKoreanDate(log.time),
                dist = stringResource(R.string.home_activity_distance_placeholder),
                time = stringResource(R.string.home_activity_time_placeholder),
                type = log.label
            )
        }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        if (isAnonymousBannerVisible) {
            item {
                AppContentCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.shapes.medium
                            )
                            .padding(appSpacingMd()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(appSpacingSm())
                        ) {
                            Text(
                                text = stringResource(R.string.home_anonymous_banner_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(R.string.home_anonymous_banner_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        IconButton(onClick = { isAnonymousBannerVisible = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
        item {
            WeeklyProgressCard(
                progress = progress,
                remainingKm = remainingKm,
                count = uiState.recordCountThisWeek,
                onClick = onGoalClickThrottled
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.home_section_weekly_manage),
                    color = textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onReminderClickThrottled,
                    colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                ) {
                    Text(
                        stringResource(R.string.home_action_reminder_settings),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.home_section_recent_activity),
                onViewAllClick = onRecordClickThrottled
            )
        }

        if (recentActivities.isEmpty()) {
            item {
                Text(
                    stringResource(
                        R.string.home_empty_recent_activity_format,
                        activityState.label
                    ),
                    color = textMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        } else {
            items(recentActivities) { activity ->
                RecentActivityRow(activity)
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(
    progress: Float,
    remainingKm: Float,
    count: Int,
    onClick: () -> Unit
) {
    val accentColor = appAccentColor()
    val surfaceColor = appSurfaceColor()
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()

    AppSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        containerColor = surfaceColor,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.home_weekly_progress_title),
                        color = textMuted,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        stringResource(
                            R.string.home_weekly_progress_complete_format,
                            (progress * 100).toInt()
                        ),
                        color = textPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = accentColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            AppProgressBar(progress = progress)

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoItem(
                    label = stringResource(R.string.home_weekly_remaining_distance),
                    value = stringResource(R.string.home_distance_km_format, remainingKm),
                    modifier = Modifier.weight(1f)
                )
                InfoItem(
                    label = stringResource(R.string.home_weekly_run_count),
                    value = stringResource(R.string.home_weekly_run_count_format, count),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()

    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(label, color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(value, color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

data class ActivityItem(val date: String, val dist: String, val time: String, val type: String)

@Composable
private fun RecentActivityRow(activity: ActivityItem) {
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.DirectionsRun,
                contentDescription = null,
                tint = textMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(
                    R.string.home_activity_title_format,
                    activity.dist,
                    activity.type
                ),
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                stringResource(
                    R.string.home_activity_subtitle_format,
                    activity.date,
                    activity.time
                ),
                color = textMuted,
                fontSize = 12.sp
            )
        }

        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun SectionHeader(title: String, onViewAllClick: () -> Unit) {
    val accentColor = appAccentColor()
    val textPrimary = appTextPrimaryColor()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = onViewAllClick) {
            Text(
                stringResource(R.string.home_action_view_all),
                color = accentColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private object PreviewDateFormatter : DateFormatter {
    override fun formatToKoreanDate(timestamp: Long): String = "2024.03.01"

    override fun formatToDistanceLabel(distanceKm: Double): String = "${distanceKm}km"

    override fun formatElapsedTime(elapsedMillis: Long): String = "00:30:00"
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val uiState = HomeUiState(
        weeklyGoalKm = 20.0,
        totalThisWeekKm = 12.5,
        recordCountThisWeek = 3,
        progress = 0.62f
    )
    val activityState = ActivityRecognitionUiState(label = "RUNNING")
    val activityLogs = listOf(
        ActivityLogUiModel(time = 1_728_000_000_000, label = "RUNNING"),
        ActivityLogUiModel(time = 1_727_900_000_000, label = "WALKING"),
        ActivityLogUiModel(time = 1_727_800_000_000, label = "RUNNING")
    )

    RunningGoalTrackerTheme {
        HomeScreen(
            uiState = uiState,
            activityState = activityState,
            activityLogs = activityLogs,
            dateFormatter = PreviewDateFormatter,
            onRecordClick = {},
            onGoalClick = {},
            onReminderClick = {}
        )
    }
}
