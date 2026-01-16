package com.jeong.runninggoaltracker.feature.reminder.notification

import android.content.Context
import androidx.core.app.NotificationCompat
import com.jeong.runninggoaltracker.feature.reminder.R
import com.jeong.runninggoaltracker.shared.designsystem.config.NumericResourceProvider
import com.jeong.runninggoaltracker.shared.designsystem.notification.NotificationPermissionGate

object ReminderNotifier {

    fun showNow(context: Context, hour: Int, minute: Int) {
        val text = context.getString(
            R.string.reminder_notification_text_format,
            hour,
            minute
        )

        val notification = NotificationCompat.Builder(context, channelId(context))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.reminder_notification_title))
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationPermissionGate.notifyIfAllowed(
            context,
            notificationId(context),
            notification
        )
    }

    private fun channelId(context: Context): String {
        return context.getString(R.string.reminder_notification_channel_id)
    }

    private fun notificationId(context: Context): Int {
        return NumericResourceProvider.reminderNotificationId(context)
    }
}
