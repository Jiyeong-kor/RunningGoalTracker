package com.jeong.runninggoaltracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.jeong.runninggoaltracker.feature.reminder.R as ReminderR
import com.jeong.runninggoaltracker.feature.reminder.notification.ReminderNotifier
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RunningGoalTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = ReminderNotifier.CHANNEL_ID
            val channelName = getString(ReminderR.string.reminder_channel_name)
            val channelDescription = getString(ReminderR.string.reminder_channel_description)

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
