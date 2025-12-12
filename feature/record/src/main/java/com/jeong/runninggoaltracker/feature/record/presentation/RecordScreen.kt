package com.jeong.runninggoaltracker.feature.record.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.record.R
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionManager
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionStateHolder
import com.jeong.runninggoaltracker.shared.designsystem.R as SharedR
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.shared.designsystem.util.toDistanceLabel
import com.jeong.runninggoaltracker.shared.designsystem.util.toKoreanDateLabel
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordRoute(
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activityState by ActivityRecognitionStateHolder.state.collectAsState()

    RecordScreen(
        uiState = uiState,
        activityLabel = activityState.label,
        onDistanceChange = viewModel::onDistanceChanged,
        onDurationChange = viewModel::onDurationChanged,
        onSaveRecord = viewModel::saveRecord
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordScreen(
    uiState: RecordUiState,
    activityLabel: String,
    onDistanceChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onSaveRecord: () -> Unit
) {
    val displayLabel = when (activityLabel) {
        "NO_PERMISSION" -> stringResource(R.string.activity_permission_needed)
        "REQUEST_FAILED", "SECURITY_EXCEPTION" ->
            stringResource(R.string.activity_recognition_failed)

        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> stringResource(R.string.activity_unknown)
        else -> activityLabel
    }

    val recordDurationLabel = stringResource(R.string.record_duration_label)
    val errorEnterNumberFormat = stringResource(R.string.error_enter_number_format)
    val errorEnterPositiveValue = stringResource(R.string.error_enter_positive_value)

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val horizontalPadding = dimensionResource(SharedR.dimen.padding_screen_horizontal)
    val verticalPadding = dimensionResource(SharedR.dimen.padding_screen_vertical)
    val sectionSpacing = dimensionResource(SharedR.dimen.spacing_screen_elements)
    val cardSpacingSmall = dimensionResource(SharedR.dimen.card_spacing_small)
    val cardSpacingMedium = dimensionResource(SharedR.dimen.card_spacing_medium)
    val cardSpacingExtraSmall = dimensionResource(SharedR.dimen.card_spacing_extra_small)
    val recordListMaxHeight = dimensionResource(SharedR.dimen.list_max_height_large)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
    ) {
        val context = LocalContext.current
        val activityManager = remember { ActivityRecognitionManager(context.applicationContext) }
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                activityManager.startUpdates()
            } else {
                ActivityRecognitionStateHolder.update("NO_PERMISSION")
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
                    onClick = {
                        if (activityManager.hasPermission()) {
                            activityManager.startUpdates()
                        } else {
                            permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.button_start_detection))
                }
                Button(
                    onClick = { activityManager.stopUpdates() },
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
                text = stringResource(R.string.record_title_add_record),
                style = typography.titleMedium
            )

            OutlinedTextField(
                value = uiState.distanceInput,
                onValueChange = onDistanceChange,
                label = { Text(stringResource(R.string.record_distance_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = uiState.durationInput,
                onValueChange = onDurationChange,
                label = { Text(recordDurationLabel) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            when (uiState.error) {
                RecordInputError.INVALID_NUMBER -> {
                    Text(
                        text = errorEnterNumberFormat,
                        color = colorScheme.error,
                        style = typography.bodyMedium
                    )
                }

                RecordInputError.NON_POSITIVE -> {
                    Text(
                        text = errorEnterPositiveValue,
                        color = colorScheme.error,
                        style = typography.bodyMedium
                    )
                }

                null -> Unit
            }

            Button(
                onClick = onSaveRecord,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_save))
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
                                        horizontalArrangement = Arrangement.spacedBy(cardSpacingExtraSmall)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarToday,
                                            contentDescription = null,
                                            tint = colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = record.date.toKoreanDateLabel(),
                                            style = typography.bodyMedium
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(cardSpacingSmall)
                                    ) {
                                        Text(
                                            text = record.distanceKm.toDistanceLabel(),
                                            style = typography.bodyMedium
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(cardSpacingExtraSmall)
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
