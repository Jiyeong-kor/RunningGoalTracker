package com.jeong.runninggoaltracker.presentation.record

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient

class ActivityRecognitionManager(
    private val context: Context
) {

    private val client: ActivityRecognitionClient =
        ActivityRecognition.getClient(context)

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

    fun startUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityRecognitionStateHolder.update("NO_PERMISSION")
                return
            }
        }

        try {
            client.requestActivityUpdates(
                INTERVAL_MILLIS,
                createPendingIntent()
            ).addOnSuccessListener {
            }.addOnFailureListener {
                ActivityRecognitionStateHolder.update("REQUEST_FAILED")
            }
        } catch (_: SecurityException) {
            ActivityRecognitionStateHolder.update("SECURITY_EXCEPTION")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopUpdates() {
        try {
            client.removeActivityUpdates(createPendingIntent())
        } catch (_: SecurityException) {
        }
        ActivityRecognitionStateHolder.update("STOPPED")
    }

    companion object {
        private const val REQUEST_CODE = 2001
        private const val INTERVAL_MILLIS = 1_000L
    }
}
