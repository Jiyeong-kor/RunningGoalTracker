package com.jeong.runninggoaltracker.feature.record.tracking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.jeong.runninggoaltracker.feature.record.api.RunningTrackerController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RunningTrackerManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val stateUpdater: RunningTrackerStateUpdater
) : RunningTrackerController {

    override fun startTracking(onPermissionRequired: () -> Unit) {
        if (!hasLocationPermission()) {
            stateUpdater.markPermissionRequired()
            onPermissionRequired()
            return
        }
        stateUpdater.markTracking()
        val intent = Intent(context, RunningTrackerService::class.java).apply {
            action = RunningTrackerService.ACTION_START
        }
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stopTracking() {
        val intent = Intent(context, RunningTrackerService::class.java).apply {
            action = RunningTrackerService.ACTION_STOP
        }
        ContextCompat.startForegroundService(context, intent)
    }

    override fun notifyPermissionDenied() {
        stateUpdater.markPermissionRequired()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
