package com.jeong.runninggoaltracker.feature.record.tracking

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jeong.runninggoaltracker.feature.record.R
import com.jeong.runninggoaltracker.feature.record.contract.RecordNotificationContract
import com.jeong.runninggoaltracker.shared.designsystem.config.NumericResourceProvider
import com.jeong.runninggoaltracker.shared.designsystem.formatter.DistanceFormatter
import com.jeong.runninggoaltracker.shared.designsystem.notification.NotificationPermissionGate
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunningNotificationDispatcher @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun createNotification(distanceKm: Double, elapsedMillis: Long): Notification {
        val elapsedMinutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)
        val channelId = RecordNotificationContract.NOTIFICATION_CHANNEL_ID
        val distanceFormatted = DistanceFormatter.formatDistanceKm(context, distanceKm)
        val content = context.getString(
            R.string.record_notification_content,
            distanceFormatted,
            elapsedMinutes
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.record_notification_title))
            .setContentText(content)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                android.R.drawable.ic_media_pause,
                context.getString(R.string.button_stop_tracking),
                createStopPendingIntent()
            )
            .build()
    }

    fun notifyProgress(distanceKm: Double, elapsedMillis: Long) =
        NotificationPermissionGate.notifyIfAllowed(
            context,
            notificationId(),
            createNotification(distanceKm, elapsedMillis)
        )

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                RecordNotificationContract.NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.record_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.record_notification_channel_description)
            }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createStopPendingIntent(): PendingIntent {
        val stopIntent = RunningTrackerService.createStopIntent(context)
        return PendingIntent.getService(
            context,
            stopRequestCode(),
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun notificationId(): Int =
        NumericResourceProvider.recordNotificationId(context)

    private fun stopRequestCode(): Int =
        NumericResourceProvider.recordStopRequestCode(context)
}
