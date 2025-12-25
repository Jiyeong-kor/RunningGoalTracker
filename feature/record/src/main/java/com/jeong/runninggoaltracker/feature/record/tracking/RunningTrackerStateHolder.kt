package com.jeong.runninggoaltracker.feature.record.tracking

import com.jeong.runninggoaltracker.feature.record.api.RunningTrackerMonitor
import com.jeong.runninggoaltracker.feature.record.api.model.RunningTrackerState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class RunningTrackerStateHolder @Inject constructor() :
    RunningTrackerMonitor,
    RunningTrackerStateUpdater {

    private val _state = MutableStateFlow(RunningTrackerState())
    override val trackerState: StateFlow<RunningTrackerState> = _state

    override fun markTracking() {
        _state.value = _state.value.copy(
            isTracking = true,
            permissionRequired = false,
            distanceKm = 0.0,
            elapsedMillis = 0L
        )
    }

    override fun update(distanceKm: Double, elapsedMillis: Long) {
        _state.value = _state.value.copy(
            distanceKm = distanceKm,
            elapsedMillis = elapsedMillis
        )
    }

    override fun stop() {
        _state.value = _state.value.copy(
            isTracking = false
        )
    }

    override fun markPermissionRequired() {
        _state.value = _state.value.copy(
            permissionRequired = true,
            isTracking = false
        )
    }
}
