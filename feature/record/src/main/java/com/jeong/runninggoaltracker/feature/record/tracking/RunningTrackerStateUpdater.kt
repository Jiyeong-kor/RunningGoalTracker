package com.jeong.runninggoaltracker.feature.record.tracking

interface RunningTrackerStateUpdater {
    fun markTracking()
    fun update(distanceKm: Double, elapsedMillis: Long)
    fun stop()
    fun markPermissionRequired()
}
