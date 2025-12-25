package com.jeong.runninggoaltracker.feature.record.api

import com.jeong.runninggoaltracker.feature.record.recognition.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityState
import kotlinx.coroutines.flow.StateFlow

interface ActivityRecognitionMonitor {
    val activityState: StateFlow<ActivityState>
    val activityLogs: StateFlow<List<ActivityLogEntry>>
}
