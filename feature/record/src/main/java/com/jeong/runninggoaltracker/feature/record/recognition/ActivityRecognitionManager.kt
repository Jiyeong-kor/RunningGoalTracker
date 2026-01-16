package com.jeong.runninggoaltracker.feature.record.recognition

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionController
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import javax.inject.Inject
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus

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
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    override fun startUpdates(onPermissionRequired: () -> Unit) {
        if (!hasPermission()) {
            activityStateUpdater.update(ActivityRecognitionStatus.NoPermission)
            onPermissionRequired()
            return
        }

        try {
            client.requestActivityUpdates(
                INTERVAL_MILLIS,
                createPendingIntent()
            ).addOnSuccessListener {
            }.addOnFailureListener {
                activityStateUpdater.update(ActivityRecognitionStatus.RequestFailed)
            }
        } catch (_: SecurityException) {
            activityStateUpdater.update(ActivityRecognitionStatus.SecurityException)
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopUpdates() {
        try {
            client.removeActivityUpdates(createPendingIntent())
        } catch (_: SecurityException) {
        }
        activityStateUpdater.update(ActivityRecognitionStatus.Stopped)
    }

    override fun notifyPermissionDenied() {
        activityStateUpdater.update(ActivityRecognitionStatus.NoPermission)
    }

    companion object {
        private const val REQUEST_CODE = 2001
        private const val INTERVAL_MILLIS = 1_000L
    }
}
