package com.jeong.runninggoaltracker.feature.record.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.record.R
import com.jeong.runninggoaltracker.feature.record.viewmodel.RecordViewModel
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.shared.designsystem.R as SharedR

@Composable
fun RecordRoute(
    onRequestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    RecordScreen(
        uiState = uiState,
        formatDistance = viewModel::formatToDistanceLabel,
        formatElapsedTime = viewModel::formatElapsedTime,
        formatDate = viewModel::formatToKoreanDate,
        onStartActivityRecognition = viewModel::startActivityRecognition,
        onStopActivityRecognition = viewModel::stopActivityRecognition,
        onPermissionDenied = viewModel::notifyPermissionDenied,
        onStartTracking = viewModel::startTracking,
        onStopTracking = viewModel::stopTracking,
        onTrackingPermissionDenied = viewModel::notifyTrackingPermissionDenied,
        onRequestTrackingPermissions = onRequestTrackingPermissions
    )
}

@Composable
fun RecordScreen(
    uiState: RecordUiState,
    formatDistance: (Double) -> String,
    formatElapsedTime: (Long) -> String,
    formatDate: (Long) -> String,
    onStartActivityRecognition: ((onPermissionRequired: () -> Unit) -> Unit),
    onStopActivityRecognition: () -> Unit,
    onPermissionDenied: () -> Unit,
    onStartTracking: ((onPermissionRequired: () -> Unit) -> Unit),
    onStopTracking: () -> Unit,
    onTrackingPermissionDenied: () -> Unit,
    onRequestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit
) {
    val displayLabel = when (uiState.activityLabel) {
        "NO_PERMISSION" -> stringResource(R.string.activity_permission_needed)
        "REQUEST_FAILED", "SECURITY_EXCEPTION" ->
            stringResource(R.string.activity_recognition_failed)

        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> stringResource(R.string.activity_unknown)
        else -> uiState.activityLabel
    }

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val horizontalPadding = dimensionResource(SharedR.dimen.padding_screen_horizontal)
    val verticalPadding = dimensionResource(SharedR.dimen.padding_screen_vertical)
    val sectionSpacing = dimensionResource(SharedR.dimen.spacing_screen_elements)
    val cardSpacingSmall = dimensionResource(SharedR.dimen.card_spacing_small)
    val cardSpacingMedium = dimensionResource(SharedR.dimen.card_spacing_medium)
    val cardSpacingExtraSmall = dimensionResource(SharedR.dimen.card_spacing_extra_small)
    val recordListMaxHeight = dimensionResource(SharedR.dimen.list_max_height_large)

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
    ) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                onStartActivityRecognition {}
            } else {
                onPermissionDenied()
            }
        }

        val startActivityRecognitionWithPermission: () -> Unit = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                onStartActivityRecognition {
                    permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            } else {
                onStartActivityRecognition {}
            }
        }

        AppContentCard(
            verticalArrangement = Arrangement.spacedBy(cardSpacingSmall)
        ) {
            Text(
                text = stringResource(R.string.record_title_activity_recognition),
                style = typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacingSmall)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = colorScheme.primary
                )
                Text(
                    text = stringResource(
                        R.string.record_current_activity_format,
                        displayLabel
                    ), style = typography.bodyLarge
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacingSmall)
            ) {
                Button(
                    onClick = startActivityRecognitionWithPermission,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_start_detection))
                }
                Button(
                    onClick = onStopActivityRecognition,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_stop_detection))
                }
            }
        }

        AppContentCard(
            verticalArrangement = Arrangement.spacedBy(cardSpacingSmall)
        ) {
            Text(
                text = stringResource(R.string.record_title_tracking),
                style = typography.titleMedium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacingSmall)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(cardSpacingExtraSmall)
                ) {
                    Text(
                        text = stringResource(R.string.record_distance_dashboard_label),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDistance(uiState.distanceKm),
                        style = typography.headlineSmall
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(cardSpacingExtraSmall)
                ) {
                    Text(
                        text = stringResource(R.string.record_elapsed_time_dashboard_label),
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatElapsedTime(uiState.elapsedMillis),
                        style = typography.headlineSmall
                    )
                }
            }

            if (uiState.permissionRequired) {
                Text(
                    text = stringResource(R.string.record_tracking_permission_required),
                    color = colorScheme.error,
                    style = typography.bodyMedium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(cardSpacingSmall)
            ) {
                Button(
                    onClick = {
                        startActivityRecognitionWithPermission()
                        onStartTracking {
                            onRequestTrackingPermissions { granted ->
                                if (granted) {
                                    onStartTracking {}
                                } else {
                                    onTrackingPermissionDenied()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isTracking
                ) {
                    Text(stringResource(R.string.button_start_tracking))
                }
                Button(
                    onClick = {
                        onStopActivityRecognition()
                        onStopTracking()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = uiState.isTracking
                ) {
                    Text(stringResource(R.string.button_stop_tracking))
                }
            }
        }

        AppContentCard(
            verticalArrangement = Arrangement.spacedBy(cardSpacingSmall)
        ) {
            Text(
                text = stringResource(R.string.record_title_saved_records),
                style = typography.titleMedium
            )

            if (uiState.records.isEmpty()) {
                Text(
                    text = stringResource(R.string.record_no_saved_records),
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(cardSpacingSmall),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = recordListMaxHeight)
                ) {
                    items(uiState.records) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = colorScheme.surfaceContainer
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
                                horizontalArrangement = Arrangement.spacedBy(cardSpacingMedium)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                    contentDescription = null,
                                    tint = colorScheme.primary
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(cardSpacingExtraSmall)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(
                                            cardSpacingExtraSmall
                                        )
                                    ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarToday,
                                        contentDescription = null,
                                        tint = colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = formatDate(record.date),
                                        style = typography.bodyMedium
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(
                                            cardSpacingSmall
                                        )
                                ) {
                                    Text(
                                        text = formatDistance(record.distanceKm),
                                        style = typography.bodyMedium
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(
                                            cardSpacingExtraSmall
                                            )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Schedule,
                                                contentDescription = null,
                                                tint = colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = stringResource(
                                                    R.string.record_duration_minutes_format,
                                                    record.durationMinutes
                                                ), style = typography.bodyMedium
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
    }
}
