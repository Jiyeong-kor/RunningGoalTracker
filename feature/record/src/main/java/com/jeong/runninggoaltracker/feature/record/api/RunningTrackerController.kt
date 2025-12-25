package com.jeong.runninggoaltracker.feature.record.api

interface RunningTrackerController {
    fun startTracking(onPermissionRequired: () -> Unit)
    fun stopTracking()
    fun notifyPermissionDenied()
}
