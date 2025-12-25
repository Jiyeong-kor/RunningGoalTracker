package com.jeong.runninggoaltracker.feature.record.recognition

import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.StateFlow

@Singleton
class ActivityRecognitionMonitorHolder @Inject constructor(
    private val stateHolder: ActivityRecognitionStateHolder
) : ActivityRecognitionMonitor {
    override val activityState: StateFlow<ActivityState>
        get() = stateHolder.state

    override val activityLogs: StateFlow<List<ActivityLogEntry>>
        get() = ActivityLogHolder.logs
}
