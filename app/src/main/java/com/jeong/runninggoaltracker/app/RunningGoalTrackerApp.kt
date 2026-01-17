package com.jeong.runninggoaltracker.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.jeong.runninggoaltracker.feature.reminder.R as ReminderR
import com.jeong.runninggoaltracker.feature.reminder.contract.ReminderNotificationContract
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RunningGoalTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = ReminderNotificationContract.NOTIFICATION_CHANNEL_ID
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
