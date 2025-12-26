package com.jeong.runninggoaltracker.app.presentation.navigation

import com.jeong.runninggoaltracker.feature.home.presentation.ActivityLogUiModel
import com.jeong.runninggoaltracker.feature.home.presentation.ActivityRecognitionUiState
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityState

internal fun ActivityState.toUiState(): ActivityRecognitionUiState =
    ActivityRecognitionUiState(label = label)

internal fun ActivityLogEntry.toUiModel(): ActivityLogUiModel =
    ActivityLogUiModel(
        time = time,
        label = label
    )
