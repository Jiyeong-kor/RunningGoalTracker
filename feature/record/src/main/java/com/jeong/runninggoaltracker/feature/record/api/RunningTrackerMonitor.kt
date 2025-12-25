package com.jeong.runninggoaltracker.feature.record.api

import com.jeong.runninggoaltracker.feature.record.api.model.RunningTrackerState
import kotlinx.coroutines.flow.StateFlow

interface RunningTrackerMonitor {
    val trackerState: StateFlow<RunningTrackerState>
}
