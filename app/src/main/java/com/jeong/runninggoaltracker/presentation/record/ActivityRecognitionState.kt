package com.jeong.runninggoaltracker.presentation.record

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ActivityState(
    val label: String = "UNKNOWN"
)

object ActivityRecognitionStateHolder {

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state

    fun update(label: String) {
        _state.value = ActivityState(
            label = label,
        )
    }
}
