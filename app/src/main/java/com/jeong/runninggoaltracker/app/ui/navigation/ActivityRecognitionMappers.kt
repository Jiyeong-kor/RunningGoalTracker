package com.jeong.runninggoaltracker.app.ui.navigation

import com.jeong.runninggoaltracker.feature.home.presentation.ActivityLogUiModel
import com.jeong.runninggoaltracker.feature.home.presentation.ActivityRecognitionUiState
import com.jeong.runninggoaltracker.feature.home.R
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityState

internal fun ActivityState.toUiState(): ActivityRecognitionUiState =
    ActivityRecognitionUiState(labelResId = status.toHomeLabelRes())

internal fun ActivityLogEntry.toUiModel(): ActivityLogUiModel =
    ActivityLogUiModel(
        time = time,
        labelResId = status.toHomeLabelRes()
    )

private fun ActivityRecognitionStatus.toHomeLabelRes(): Int {
    return when (this) {
        ActivityRecognitionStatus.NoPermission -> R.string.activity_permission_needed
        ActivityRecognitionStatus.RequestFailed,
        ActivityRecognitionStatus.SecurityException -> R.string.activity_recognition_failed
        ActivityRecognitionStatus.Stopped -> R.string.activity_stopped
        ActivityRecognitionStatus.NoResult,
        ActivityRecognitionStatus.NoActivity,
        ActivityRecognitionStatus.Unknown -> R.string.activity_unknown
        ActivityRecognitionStatus.Running -> R.string.activity_running
        ActivityRecognitionStatus.Walking -> R.string.activity_walking
        ActivityRecognitionStatus.OnBicycle -> R.string.activity_on_bicycle
        ActivityRecognitionStatus.InVehicle -> R.string.activity_in_vehicle
        ActivityRecognitionStatus.Still -> R.string.activity_still
    }
}
