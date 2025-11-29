package com.jeong.runninggoaltracker.presentation.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import java.util.Calendar

class ReminderAlarmScheduler(
    private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    private fun createPendingIntent(hour: Int, minute: Int): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("hour", hour)
            putExtra("minute", minute)
        }

        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * 정확 알람 권한이 있으면 setExactAndAllowWhileIdle
     * 권한이 없으면 setAndAllowWhileIdle 로 degrade
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun schedule(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 이미 지난 시각이면 내일로 이동
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val triggerAtMillis = calendar.timeInMillis
        val pendingIntent = createPendingIntent(hour, minute)

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // 정확 알람 허용됨
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    // 정확 알람 불가 → 부정확하지만 동작은 하도록 degrade
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                // Android 11 이하: 정확 알람 허용
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            // 권한 없음 → 부정확 알람으로 degrade
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancel(hour: Int, minute: Int) {
        val pendingIntent = createPendingIntent(hour, minute)
        alarmManager.cancel(pendingIntent)
    }

    companion object {
        private const val REQUEST_CODE = 1002
    }
}
