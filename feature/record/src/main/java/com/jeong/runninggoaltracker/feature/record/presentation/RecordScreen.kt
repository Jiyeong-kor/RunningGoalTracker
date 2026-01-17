package com.jeong.runninggoaltracker.feature.record.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.record.R
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus
import com.jeong.runninggoaltracker.feature.record.viewmodel.RecordViewModel
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.appAccentColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appBackgroundColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appOnAccentColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSurfaceColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextMutedColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextPrimaryColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import com.jeong.runninggoaltracker.shared.designsystem.config.NumericResourceProvider
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun RecordRoute(
    onRequestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit,
    onRequestActivityRecognitionPermission: (onResult: (Boolean) -> Unit) -> Unit,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    RecordScreen(
        uiState = uiState,
        onStartActivityRecognition = viewModel::startActivityRecognition,
        onStopActivityRecognition = viewModel::stopActivityRecognition,
        onPermissionDenied = viewModel::notifyPermissionDenied,
        onStartTracking = viewModel::startTracking,
        onStopTracking = viewModel::stopTracking,
        onTrackingPermissionDenied = viewModel::notifyTrackingPermissionDenied,
        onRequestActivityRecognitionPermission = onRequestActivityRecognitionPermission,
        onRequestTrackingPermissions = onRequestTrackingPermissions
    )
}

@Composable
fun RecordScreen(
    uiState: RecordUiState,
    onStartActivityRecognition: ((onPermissionRequired: () -> Unit) -> Unit),
    onStopActivityRecognition: () -> Unit,
    onPermissionDenied: () -> Unit,
    onStartTracking: ((onPermissionRequired: () -> Unit) -> Unit),
    onStopTracking: () -> Unit,
    onTrackingPermissionDenied: () -> Unit,
    onRequestActivityRecognitionPermission: (onResult: (Boolean) -> Unit) -> Unit,
    onRequestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit
) {
    val displayLabel = uiState.activityStatus.toRecordLabel()
    val context = LocalContext.current
    val zeroLong = NumericResourceProvider.zeroLong(context)
    val zeroDouble = NumericResourceProvider.zeroDouble(context)
    val secondsPerMinute = integerResource(R.integer.record_seconds_per_minute)
    val alphaDenominator = integerResource(R.integer.record_alpha_denominator)
    val fullWeight = integerResource(R.integer.record_weight_full).toFloat()
    val accentAlphaWeak = integerResource(R.integer.record_accent_alpha_weak).toFloat() /
            alphaDenominator.toFloat()
    val accentAlphaStrong = integerResource(R.integer.record_accent_alpha_strong).toFloat() /
            alphaDenominator.toFloat()

    val startActivityRecognitionWithPermission: () -> Unit = {
        onStartActivityRecognition {
            onRequestActivityRecognitionPermission { granted ->
                if (granted) {
                    onStartActivityRecognition {}
                } else {
                    onPermissionDenied()
                }
            }
        }
    }

    val startTrackingWithPermission: () -> Unit = {
        onStartTracking {
            onRequestTrackingPermissions { granted ->
                if (granted) {
                    onStartTracking {}
                } else {
                    onTrackingPermissionDenied()
                }
            }
        }
    }

    val onPauseClick = rememberThrottleClick(onClick = {
        if (uiState.isTracking) {
            onStopActivityRecognition()
            onStopTracking()
        } else {
            startActivityRecognitionWithPermission()
            startTrackingWithPermission()
        }
    })

    val onStopClick = rememberThrottleClick(onClick = {
        onStopActivityRecognition()
        onStopTracking()
    })

    val distanceValue = stringResource(R.string.record_distance_format, uiState.distanceKm)
    val accentColor = appAccentColor()
    val backgroundColor = appBackgroundColor()
    val surfaceColor = appSurfaceColor()
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()
    val onAccent = appOnAccentColor()
    val paceLabel = formatPace(
        distanceKm = uiState.distanceKm,
        elapsedMillis = uiState.elapsedMillis,
        paceZero = stringResource(R.string.record_pace_zero),
        paceFormat = stringResource(R.string.record_pace_format),
        zeroDouble = zeroDouble,
        zeroLong = zeroLong,
        secondsPerMinute = secondsPerMinute
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(dimensionResource(R.dimen.record_screen_padding)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.record_top_spacer_height)))

        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(dimensionResource(R.dimen.record_circle_size)),
                shape = CircleShape,
                color = accentColor.copy(alpha = accentAlphaWeak),
                border = androidx.compose.foundation.BorderStroke(
                    dimensionResource(R.dimen.record_circle_border),
                    accentColor.copy(alpha = accentAlphaStrong)
                )
            ) {}

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    displayLabel.uppercase(Locale.getDefault()),
                    color = accentColor,
                    fontSize = dimensionResource(R.dimen.record_label_font_size).value.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = dimensionResource(R.dimen.record_label_letter_spacing).value.sp
                )
                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(R.dimen.record_label_spacer_height)
                    )
                )
                Text(
                    distanceValue,
                    color = textPrimary,
                    fontSize = dimensionResource(R.dimen.record_distance_font_size).value.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.record_unit_kilometers),
                    color = textMuted,
                    fontSize = dimensionResource(R.dimen.record_unit_font_size).value.sp
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(dimensionResource(R.dimen.record_metric_spacing))
        ) {
            MetricItem(
                label = stringResource(R.string.record_metric_time),
                value = formatElapsedTimeLabel(uiState.elapsedMillis, zeroLong),
                modifier = Modifier.weight(fullWeight)
            )
            MetricItem(
                label = stringResource(R.string.record_metric_pace),
                value = paceLabel,
                modifier = Modifier.weight(fullWeight)
            )
            MetricItem(
                label = stringResource(R.string.record_metric_calories),
                value = stringResource(R.string.record_calories_zero),
                modifier = Modifier.weight(fullWeight)
            )
        }

        if (uiState.permissionRequired) {
            Text(
                text = stringResource(R.string.record_tracking_permission_required),
                color = MaterialTheme.colorScheme.error,
                fontSize = dimensionResource(R.dimen.record_permission_font_size).value.sp
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.record_control_row_bottom_padding)),
            horizontalArrangement =
                Arrangement.spacedBy(dimensionResource(R.dimen.record_metric_spacing))
        ) {
            RecordControlButton(
                label = if (uiState.isTracking) {
                    stringResource(R.string.record_action_pause)
                } else {
                    stringResource(R.string.record_action_start)
                },
                icon = Icons.Default.Pause,
                containerColor = if (uiState.isTracking) surfaceColor else accentColor,
                contentColor = if (uiState.isTracking) textPrimary else onAccent,
                modifier = Modifier.weight(fullWeight),
                onClick = onPauseClick
            )
            RecordControlButton(
                label = stringResource(R.string.record_action_stop),
                icon = Icons.Default.Stop,
                containerColor = colorResource(R.color.record_stop_button_color),
                contentColor = onAccent,
                modifier = Modifier.weight(fullWeight),
                onClick = onStopClick
            )
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String, modifier: Modifier = Modifier) {
    val alphaDenominator = integerResource(R.integer.record_alpha_denominator)
    val metricBackgroundAlpha =
        integerResource(R.integer.record_metric_background_alpha).toFloat() /
                alphaDenominator.toFloat()
    Column(
        modifier = modifier
            .background(
                Color.White.copy(alpha = metricBackgroundAlpha),
                RoundedCornerShape(dimensionResource(R.dimen.record_metric_item_shape))
            )
            .padding(dimensionResource(R.dimen.record_metric_item_padding)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            color = appTextMutedColor(),
            fontSize = dimensionResource(R.dimen.record_metric_label_text_size).value.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(
            modifier = Modifier.height(
                dimensionResource(R.dimen.record_metric_label_spacing)
            )
        )
        Text(
            value,
            color = appTextPrimaryColor(),
            fontSize = dimensionResource(R.dimen.record_metric_value_text_size).value.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun RecordControlButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(dimensionResource(R.dimen.record_control_button_height)),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.record_control_button_shape)),
        contentPadding = PaddingValues(dimensionResource(R.dimen.record_control_button_padding))
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.record_control_button_icon_size))
        )
        Spacer(
            modifier = Modifier.width(
                dimensionResource(R.dimen.record_control_button_icon_spacing)
            )
        )
        Text(
            label,
            fontSize = dimensionResource(R.dimen.record_control_button_text_size).value.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ActivityRecognitionStatus.toRecordLabel(): String =
    when (this) {
        ActivityRecognitionStatus.NoPermission ->
            stringResource(R.string.activity_permission_needed)

        ActivityRecognitionStatus.RequestFailed,
        ActivityRecognitionStatus.SecurityException ->
            stringResource(R.string.activity_recognition_failed)

        ActivityRecognitionStatus.Stopped ->
            stringResource(R.string.activity_stopped)

        ActivityRecognitionStatus.NoResult,
        ActivityRecognitionStatus.NoActivity,
        ActivityRecognitionStatus.Unknown ->
            stringResource(R.string.activity_unknown)

        ActivityRecognitionStatus.Running ->
            stringResource(R.string.activity_running)

        ActivityRecognitionStatus.Walking ->
            stringResource(R.string.activity_walking)

        ActivityRecognitionStatus.OnBicycle ->
            stringResource(R.string.activity_on_bicycle)

        ActivityRecognitionStatus.InVehicle ->
            stringResource(R.string.activity_in_vehicle)

        ActivityRecognitionStatus.Still ->
            stringResource(R.string.activity_still)
    }

private fun formatPace(
    distanceKm: Double,
    elapsedMillis: Long,
    paceZero: String,
    paceFormat: String,
    zeroDouble: Double,
    zeroLong: Long,
    secondsPerMinute: Int
): String {
    if (distanceKm <= zeroDouble || elapsedMillis <= zeroLong) {
        return paceZero
    }
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)
    val secondsPerKm = totalSeconds.toDouble() / distanceKm
    val minutes = (secondsPerKm / secondsPerMinute).toInt()
    val seconds = (secondsPerKm % secondsPerMinute).toInt()
    return String.format(Locale.getDefault(), paceFormat, minutes, seconds)
}

@Composable
private fun formatElapsedTimeLabel(elapsedMillis: Long, zeroLong: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis)
    val hours = TimeUnit.SECONDS.toHours(totalSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) - TimeUnit.HOURS.toMinutes(hours)
    val seconds =
        totalSeconds - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(totalSeconds))
    return if (hours > zeroLong) {
        stringResource(R.string.record_elapsed_time_hms_format, hours, minutes, seconds)
    } else {
        stringResource(R.string.record_elapsed_time_ms_format, minutes, seconds)
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordScreenPreview() {
    val uiState = RecordUiState(
        activityStatus = ActivityRecognitionStatus.Running,
        isTracking = true,
        distanceKm = 3.45,
        elapsedMillis = 1_245_000,
        permissionRequired = false
    )

    RunningGoalTrackerTheme {
        RecordScreen(
            uiState = uiState,
            onStartActivityRecognition = { _ -> },
            onStopActivityRecognition = {},
            onPermissionDenied = {},
            onStartTracking = { _ -> },
            onStopTracking = {},
            onTrackingPermissionDenied = {},
            onRequestActivityRecognitionPermission = {},
            onRequestTrackingPermissions = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MetricItemPreview() {
    RunningGoalTrackerTheme {
        MetricItem(
            label = "시간",
            value = "00:45:12"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordControlButtonPreview() {
    RunningGoalTrackerTheme {
        RecordControlButton(
            label = "일시정지",
            icon = Icons.Default.Pause,
            containerColor = appAccentColor(),
            contentColor = appOnAccentColor(),
            onClick = {}
        )
    }
}
