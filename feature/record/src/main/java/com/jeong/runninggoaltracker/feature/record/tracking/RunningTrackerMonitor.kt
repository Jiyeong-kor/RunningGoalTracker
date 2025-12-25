package com.jeong.runninggoaltracker.feature.record.tracking

import kotlinx.coroutines.flow.StateFlow

interface RunningTrackerMonitor {
    val trackerState: StateFlow<RunningTrackerState>
}
