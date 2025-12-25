package com.jeong.runninggoaltracker.feature.record.api

import com.jeong.runninggoaltracker.feature.record.api.model.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityState
import kotlinx.coroutines.flow.StateFlow

interface ActivityRecognitionMonitor {
    val activityState: StateFlow<ActivityState>
    val activityLogs: StateFlow<List<ActivityLogEntry>>
}
