package com.jeong.runninggoaltracker.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.R
import com.jeong.runninggoaltracker.shared.designsystem.R as SharedR
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.presentation.record.ActivityLogHolder
import com.jeong.runninggoaltracker.presentation.record.ActivityRecognitionStateHolder
import com.jeong.runninggoaltracker.util.toDistanceLabel
import com.jeong.runninggoaltracker.util.toKoreanDateLabel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onRecordClick: () -> Unit,
    onGoalClick: () -> Unit,
    onReminderClick: () -> Unit
) {

    val state by viewModel.uiState.collectAsState()
    val activityState by ActivityRecognitionStateHolder.state.collectAsState()
    val activityLogs by ActivityLogHolder.logs.collectAsState()

    val rawLabel = activityState.label
    val activityLabel = when (rawLabel) {
        "NO_PERMISSION" -> stringResource(R.string.activity_permission_needed)
        "REQUEST_FAILED", "SECURITY_EXCEPTION" ->
            stringResource(R.string.activity_recognition_failed)

        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> stringResource(R.string.activity_unknown)
        else -> rawLabel
    }

    val weeklyGoalKm = state.weeklyGoalKm
    val totalThisWeekKm = state.totalThisWeekKm

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
                    .wrapContentWidth()
                    .clip(
                        RoundedCornerShape(
                            dimensionResource(SharedR.dimen.chip_corner_radius)
                        )
                    )
                    .background(activityChipColor)
                    .padding(
                        horizontal = dimensionResource(SharedR.dimen.chip_padding_horizontal),
                        vertical = dimensionResource(SharedR.dimen.chip_padding_vertical)
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
                    text = activityLabel,
                    style = typography.bodyMedium,
                    color = colorScheme.onPrimaryContainer
                )
            }

            if (weeklyGoalKm != null) {
                Text(
                    text = stringResource(
                        R.string.home_weekly_goal_format,
                        weeklyGoalKm.toDistanceLabel()
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
                    totalThisWeekKm.toDistanceLabel()
                ), style = typography.bodyMedium
            )
            Text(
                text = stringResource(
                    R.string.home_running_count_this_week_format,
                    state.recordCountThisWeek
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
                    progress = { state.progress },
                    modifier = Modifier.matchParentSize()
                )
                Text(
                    text = stringResource(
                        R.string.home_progress_format,
                        (state.progress * 100).toInt()
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
            val lastLogs = activityLogs.takeLast(5).asReversed()

            AppContentCard {
                Text(
                    text = stringResource(R.string.home_title_recent_activity_logs),
                    style = typography.titleMedium
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)
                ) {
                    items(lastLogs) { log ->
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
                                    text = log.label,
                                    style = typography.bodyLarge
                                )
                                Text(
                                    text = log.time.toKoreanDateLabel(),
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
