package com.jeong.runninggoaltracker.feature.record.recognition

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionController
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import javax.inject.Inject
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus
import com.jeong.runninggoaltracker.shared.designsystem.config.NumericResourceProvider

class ActivityRecognitionManager @Inject constructor(
    private val context: Context,
    private val activityStateUpdater: ActivityStateUpdater
) : ActivityRecognitionController {

    private val client: ActivityRecognitionClient =
        ActivityRecognition.getClient(context)

    private fun hasPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(
            context.applicationContext, ActivityRecognitionReceiver::class.java
        )
        return PendingIntent.getBroadcast(
            context.applicationContext,
            requestCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    override fun startUpdates(onPermissionRequired: () -> Unit) {
        if (!hasPermission()) {
            activityStateUpdater.update(ActivityRecognitionStatus.NoPermission)
            onPermissionRequired()
            return
        }

        requestUpdatesWithPermission()
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    override fun stopUpdates() {
        if (!hasPermission()) {
            activityStateUpdater.update(ActivityRecognitionStatus.NoPermission)
            activityStateUpdater.update(ActivityRecognitionStatus.Stopped)
            return
        }
        removeUpdatesWithPermission()
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACTIVITY_RECOGNITION, "com.google.android.gms.permission.ACTIVITY_RECOGNITION"])
    private fun requestUpdatesWithPermission() {
        try {
            client.requestActivityUpdates(
                intervalMillis(),
                createPendingIntent()
            ).addOnSuccessListener {
            }.addOnFailureListener {
                activityStateUpdater.update(ActivityRecognitionStatus.RequestFailed)
            }
        } catch (_: SecurityException) {
            activityStateUpdater.update(ActivityRecognitionStatus.SecurityException)
        }
    }

    @RequiresPermission(anyOf = [Manifest.permission.ACTIVITY_RECOGNITION, "com.google.android.gms.permission.ACTIVITY_RECOGNITION"])
    private fun removeUpdatesWithPermission() {
        try {
            client.removeActivityUpdates(createPendingIntent())
        } catch (_: SecurityException) {
        }
        activityStateUpdater.update(ActivityRecognitionStatus.Stopped)
    }

    override fun notifyPermissionDenied() {
        activityStateUpdater.update(ActivityRecognitionStatus.NoPermission)
    }

    private fun requestCode(): Int {
        return NumericResourceProvider.activityRecognitionRequestCode(context)
    }

    private fun intervalMillis(): Long {
        return NumericResourceProvider.activityRecognitionIntervalMillis(context)
    }
}
