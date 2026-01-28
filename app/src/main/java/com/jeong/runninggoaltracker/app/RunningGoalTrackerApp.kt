package com.jeong.runninggoaltracker.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import com.jeong.runninggoaltracker.app.contract.KakaoSdkContract
import com.jeong.runninggoaltracker.feature.reminder.R as ReminderR
import com.jeong.runninggoaltracker.feature.reminder.contract.ReminderNotificationContract
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class RunningGoalTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        readKakaoNativeAppKey()?.let { appKey ->
            KakaoSdk.init(this, appKey)
        }
        createNotificationChannel()
    }

    private fun readKakaoNativeAppKey(): String? {
        val applicationInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )
        return applicationInfo.metaData?.getString(KakaoSdkContract.META_DATA_APP_KEY)
            ?.takeIf { it.isNotBlank() }
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
