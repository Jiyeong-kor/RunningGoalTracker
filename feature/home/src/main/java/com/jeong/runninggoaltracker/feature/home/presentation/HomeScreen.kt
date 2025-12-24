package com.jeong.runninggoaltracker.feature.home.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.domain.util.DateFormatter
import com.jeong.runninggoaltracker.feature.home.R
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import kotlinx.coroutines.flow.Flow
import com.jeong.runninggoaltracker.shared.designsystem.R as SharedR

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
    dateFormatter : DateFormatter,
    onRecordClick: () -> Unit,
    onGoalClick: () -> Unit,
    onReminderClick: () -> Unit
) {
    val rawLabel = activityState.label
    val activityLabel = when (rawLabel) {
        "NO_PERMISSION" -> stringResource(R.string.activity_permission_needed)
        "REQUEST_FAILED", "SECURITY_EXCEPTION" ->
            stringResource(R.string.activity_recognition_failed)

        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> stringResource(R.string.activity_unknown)
        else -> rawLabel
    }

    val weeklyGoalKm = uiState.weeklyGoalKm
    val totalThisWeekKm = uiState.totalThisWeekKm

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val activityChipColor by animateColorAsState(
        targetValue = when (rawLabel) {
            "RUNNING" -> colorScheme.tertiaryContainer
            "WALKING" -> colorScheme.primaryContainer
            "STILL" -> colorScheme.surfaceVariant
            else -> colorScheme.surfaceContainerLow
        },
        label = "activityChipColor"
    )

    val activityIcon = when (rawLabel) {
        "RUNNING" -> Icons.AutoMirrored.Filled.DirectionsRun
        "WALKING" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "STILL" -> Icons.Filled.SelfImprovement
        else -> Icons.AutoMirrored.Filled.HelpOutline
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = dimensionResource(SharedR.dimen.padding_screen_horizontal),
                vertical = dimensionResource(SharedR.dimen.padding_screen_vertical)
            )
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(SharedR.dimen.spacing_screen_elements)
        )
    ) {
        AppContentCard(
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(SharedR.dimen.card_spacing_medium)
            )
        ) {
            Text(
                text = stringResource(R.string.home_title_today_status),
                style = typography.titleMedium,
                color = colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(999.dp))
                    .background(activityChipColor)
                    .padding(
                        horizontal = dimensionResource(SharedR.dimen.card_spacing_medium),
                        vertical = dimensionResource(SharedR.dimen.card_spacing_small)
                    ),
                horizontalArrangement = Arrangement.spacedBy(
                    dimensionResource(SharedR.dimen.card_spacing_small)
                )
            ) {
                Icon(
                    imageVector = activityIcon,
                    contentDescription = stringResource(
                        R.string.content_description_current_activity
                    ),
                    tint = colorScheme.onPrimaryContainer
                )
                Text(
                    modifier = Modifier.testTag("currentActivityText"),
                    text = activityLabel,
                    style = typography.bodyMedium,
                    color = colorScheme.onPrimaryContainer
                )
            }

            if (weeklyGoalKm != null) {
                Text(
                    text = stringResource(
                        R.string.home_weekly_goal_format,
                        dateFormatter.formatToDistanceLabel(weeklyGoalKm)
                    ), style = typography.bodyLarge
                )
            } else {
                Text(
                    text = stringResource(R.string.home_weekly_goal_not_set),
                    style = typography.bodyLarge
                )
            }

            Text(
                text = stringResource(
                    R.string.home_total_distance_this_week_format,
                    dateFormatter.formatToDistanceLabel(totalThisWeekKm)
                ), style = typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.home_running_count_this_week_format,
                    uiState.recordCountThisWeek
                ), style = typography.bodyMedium
            )

            Spacer(Modifier.height(dimensionResource(SharedR.dimen.card_spacing_small)))

            Box(
                Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
                    .align(Alignment.CenterHorizontally)
            ) {
                CircularProgressIndicator(
                    progress = { uiState.progress },
                    modifier = Modifier.matchParentSize()
                )
                Text(
                    text = stringResource(
                        R.string.home_progress_format,
                        (uiState.progress * 100).toInt()
                    ), style = typography.titleMedium,
                    color = colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        AppContentCard {
            Button(
                onClick = onRecordClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_add_view_record))
            }

            Button(
                onClick = onGoalClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_set_weekly_goal))
            }

            Button(
                onClick = onReminderClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_set_reminder))
            }
        }

        if (activityLogs.isNotEmpty()) {
            val lastLogs = activityLogs.reversed().take(5)

            AppContentCard {
                Text(
                    text = stringResource(R.string.home_title_recent_activity_logs),
                    style = typography.titleMedium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(SharedR.dimen.list_spacing_small)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    lastLogs.forEach { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surfaceContainerHigh
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                            shape = RoundedCornerShape(
                                dimensionResource(SharedR.dimen.log_card_corner_radius)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(SharedR.dimen.log_card_padding)),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier.testTag("activityLogLabelText_${log.label}"),
                                    text = log.label,
                                    style = typography.bodyLarge
                                )
                                Text(
                                    text = dateFormatter.formatToKoreanDate(log.time),
                                    style = typography.labelSmall,
                                    color = colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
