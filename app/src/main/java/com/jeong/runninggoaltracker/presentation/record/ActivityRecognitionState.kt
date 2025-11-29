package com.jeong.runninggoaltracker.presentation.record

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ActivityState(
    val label: String = "UNKNOWN",
    val confidence: Int = 0
)

object ActivityRecognitionStateHolder {

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state

    fun update(label: String, confidence: Int) {
        _state.value = ActivityState(
            label = label,
            confidence = confidence
        )
    }
}
