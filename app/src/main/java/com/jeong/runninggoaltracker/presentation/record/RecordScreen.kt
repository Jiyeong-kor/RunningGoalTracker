package com.jeong.runninggoaltracker.presentation.record

import android.os.Build
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.R
import com.jeong.runninggoaltracker.shared.designsystem.R as SharedR
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.util.toDistanceLabel
import com.jeong.runninggoaltracker.util.toKoreanDateLabel
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordScreen(
    viewModel: RecordViewModel = hiltViewModel()
) {
    var distanceText by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val records by viewModel.records.collectAsState()

    val context = LocalContext.current
    val activityManager = remember { ActivityRecognitionManager(context.applicationContext) }
    val activityState by ActivityRecognitionStateHolder.state.collectAsState()

    val displayLabel = when (activityState.label) {
        "NO_PERMISSION" -> stringResource(R.string.activity_permission_needed)
        "REQUEST_FAILED", "SECURITY_EXCEPTION" ->
            stringResource(R.string.activity_recognition_failed)

        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> stringResource(R.string.activity_unknown)
        else -> activityState.label
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
                    onClick = { activityManager.startUpdates() },
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
                value = distanceText,
                onValueChange = {
                    distanceText = it
                    errorText = null
                },
                label = { Text(stringResource(R.string.record_distance_label)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = durationText,
                onValueChange = {
                    durationText = it
                    errorText = null
                },
                label = { Text(recordDurationLabel) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (errorText != null) {
                Text(
                    text = errorText!!,
                    color = colorScheme.error,
                    style = typography.bodyMedium
                )
            }

            Button(
                onClick = {
                    val distance = distanceText.toDoubleOrNull()
                    val duration = durationText.toIntOrNull()

                    when {
                        distance == null || duration == null -> {
                            errorText = errorEnterNumberFormat
                        }

                        distance <= 0.0 || duration <= 0 -> {
                            errorText = errorEnterPositiveValue
                        }

                        else -> {
                            val todayString = LocalDate.now().toString()
                            viewModel.addRecord(
                                dateString = todayString,
                                distanceKm = distance,
                                durationMinutes = duration
                            )
                            distanceText = ""
                            durationText = ""
                            errorText = null
                        }
                    }
                },
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

            if (records.isEmpty()) {
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
                    items(records) { record ->
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
