package com.jeong.runninggoaltracker.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.R
import com.jeong.runninggoaltracker.presentation.common.toDistanceLabel
import com.jeong.runninggoaltracker.presentation.common.toKoreanDateLabel
import com.jeong.runninggoaltracker.presentation.record.ActivityLogHolder
import com.jeong.runninggoaltracker.presentation.record.ActivityRecognitionStateHolder

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
        "REQUEST_FAILED", "SECURITY_EXCEPTION" -> stringResource(R.string.activity_recognition_failed)
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
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_title_today_status),
                    style = typography.titleMedium,
                    color = colorScheme.onSurface
                )

                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(activityChipColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = activityIcon,
                        contentDescription = stringResource(R.string.content_description_current_activity),
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

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = stringResource(
                        R.string.home_progress_format,
                        (state.progress * 100).toInt()
                    ), style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_title_quick_menu),
                    style = typography.titleMedium
                )

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
        }

        if (activityLogs.isNotEmpty()) {
            val lastLogs = activityLogs.takeLast(5).asReversed()

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceContainerLow
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${log.label} (${log.confidence}%)",
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
}
