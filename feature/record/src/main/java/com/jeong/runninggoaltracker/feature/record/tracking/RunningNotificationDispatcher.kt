package com.jeong.runninggoaltracker.feature.record.tracking

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.jeong.runninggoaltracker.feature.record.R
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningNotificationDispatcher @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun createNotification(distanceKm: Double, elapsedMillis: Long): Notification {
        val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
        val content = context.getString(
            R.string.record_notification_content,
            String.format(Locale.getDefault(), "%.2f", distanceKm),
            elapsedMinutes
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.record_notification_title))
            .setContentText(content)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.button_stop_tracking),
                createStopPendingIntent()
            )
            .build()
    }

    @SuppressLint("MissingPermission")
    fun notifyProgress(distanceKm: Double, elapsedMillis: Long) {
        if (canPostNotifications()) {
            NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID, createNotification(distanceKm, elapsedMillis))
        }
    }

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.record_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.record_notification_channel_description)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun canPostNotifications(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createStopPendingIntent(): PendingIntent {
        val stopIntent = RunningTrackerService.createStopIntent(context)
        return PendingIntent.getService(
            context,
            REQUEST_CODE_STOP,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @VisibleForTesting
    internal fun canNotifyForTest(): Boolean = canPostNotifications()

    companion object {
        const val NOTIFICATION_ID = 4001
        const val CHANNEL_ID = "running_tracker_channel"
        private const val REQUEST_CODE_STOP = 4002
    }
}
