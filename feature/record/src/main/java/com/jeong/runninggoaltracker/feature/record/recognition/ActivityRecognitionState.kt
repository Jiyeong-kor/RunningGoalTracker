package com.jeong.runninggoaltracker.feature.record.recognition

import com.jeong.runninggoaltracker.feature.record.api.model.ActivityState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ActivityStateUpdater {
    fun update(label: String)
}

@Singleton
class ActivityRecognitionStateHolder @Inject constructor() : ActivityStateUpdater {

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state

    override fun update(label: String) {
        _state.value = ActivityState(
            label = label,
        )
    }
}
