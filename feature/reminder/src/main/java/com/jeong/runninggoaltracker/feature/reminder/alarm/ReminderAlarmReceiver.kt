package com.jeong.runninggoaltracker.feature.reminder.alarm

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.jeong.runninggoaltracker.feature.reminder.notification.ReminderNotifier

class ReminderAlarmReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", 0)
        val hour = intent.getIntExtra("hour", 0)
        val minute = intent.getIntExtra("minute", 0)
        val dayOfWeek = intent.getIntExtra("dayOfWeek", 0)

        Log.d(
            "ReminderAlarmReceiver",
            "onReceive(): id=$id, hour=$hour, minute=$minute, dayOfWeek=$dayOfWeek, intent=$intent"
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                Log.d(
                    "ReminderAlarmReceiver",
                    "POST_NOTIFICATIONS 권한이 허용되지 않아 알림을 건너뜁니다"
                )
                return
            }
        }

        ReminderNotifier.showNow(context, hour, minute)

        if (id != 0 && dayOfWeek != 0) {
            val scheduler = ReminderAlarmScheduler(context)
            scheduler.schedule(id, hour, minute, setOf(dayOfWeek))
            Log.d(
                "ReminderAlarmReceiver",
                "다음 exact alarm을 다시 예약합니다: id=$id, hour=$hour, minute=$minute, dayOfWeek=$dayOfWeek"
            )
        }
    }
}
