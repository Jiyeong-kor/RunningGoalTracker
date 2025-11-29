package com.jeong.runninggoaltracker.presentation.reminder

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jeong.runninggoaltracker.R

object ReminderNotifier {

    private const val CHANNEL_ID = "running_reminder"
    private const val NOTIFICATION_ID = 1001

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("DefaultLocale")
    fun showNow(context: Context, hour: Int, minute: Int) {
        val text = String.format("%02d:%02d 러닝할 시간이에요!", hour, minute)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // 프로젝트에 맞게 아이콘 사용
            .setContentTitle("러닝 리마인더")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
