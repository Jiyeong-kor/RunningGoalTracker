package com.jeong.runninggoaltracker.feature.record.recognition

import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface ActivityRecognitionMonitor {
    val activityState: StateFlow<ActivityState>
    val activityLogs: StateFlow<List<ActivityLogEntry>>
}

@Singleton
class ActivityRecognitionMonitorHolder @Inject constructor(
    private val stateHolder: ActivityRecognitionStateHolder
) : ActivityRecognitionMonitor {
    override val activityState: StateFlow<ActivityState>
        get() = stateHolder.state

    override val activityLogs: StateFlow<List<ActivityLogEntry>>
        get() = ActivityLogHolder.logs
}
