package com.jeong.runninggoaltracker.feature.record.recognition

import com.jeong.runninggoaltracker.feature.record.api.model.ActivityState
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ActivityStateUpdater {
    fun update(status: ActivityRecognitionStatus)
}

@Singleton
class ActivityRecognitionStateHolder @Inject constructor() : ActivityStateUpdater {

    private val _state = MutableStateFlow(ActivityState())
    val state: StateFlow<ActivityState> = _state

    override fun update(status: ActivityRecognitionStatus) {
        _state.value = ActivityState(
            status = status,
        )
    }
}
