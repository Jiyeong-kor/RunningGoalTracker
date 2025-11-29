package com.jeong.runninggoaltracker.presentation.reminder

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission

class ReminderAlarmReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val hour = intent.getIntExtra("hour", 20)
        val minute = intent.getIntExtra("minute", 0)

        // 알림 실제 표시
        ReminderNotifier.showNow(context, hour, minute)
    }
}
