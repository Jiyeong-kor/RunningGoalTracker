package com.jeong.runninggoaltracker.feature.record.api

interface ActivityRecognitionController {
    fun startUpdates(onPermissionRequired: () -> Unit)
    fun stopUpdates()
    fun notifyPermissionDenied()
}
