package com.jeong.runninggoaltracker.feature.reminder.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderAlarmScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    private fun getUniqueRequestCode(id: Int, hour: Int, minute: Int, dayOfWeek: Int): Int =
        REQUEST_CODE_BASE + id * 10000 + hour * 100 + minute * 10 + dayOfWeek

    private fun createPendingIntent(
        id: Int,
        hour: Int,
        minute: Int,
        dayOfWeek: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = "com.jeong.runninggoaltracker.REMINDER_ALARM_${id}_${dayOfWeek}"

            putExtra("id", id)
            putExtra("hour", hour)
            putExtra("minute", minute)
            putExtra("dayOfWeek", dayOfWeek)
        }

        return PendingIntent.getBroadcast(
            context,
            getUniqueRequestCode(id, hour, minute, dayOfWeek),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun schedule(id: Int?, hour: Int, minute: Int, days: Set<Int>) {
        val nonNullId = id ?: return
        if (days.isEmpty()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val canExact = alarmManager.canScheduleExactAlarms()
            Log.d("ReminderAlarmScheduler", "정확한 알람 예약 가능 여부 = $canExact")
            if (!canExact) {
                Log.w(
                    "ReminderAlarmScheduler",
                    "정확한 알람을 예약할 수 없습니다. 예약을 건너뜁니다."
                )
                return
            }
        }

        days.forEach { dayOfWeek ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_WEEK, dayOfWeek)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }

            val triggerAtMillis = calendar.timeInMillis
            val pendingIntent = createPendingIntent(nonNullId, hour, minute, dayOfWeek)

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancel(id: Int?, hour: Int, minute: Int, days: Set<Int>) {
        val nonNullId = id ?: return

        days.forEach { dayOfWeek ->
            val pendingIntent = createPendingIntent(nonNullId, hour, minute, dayOfWeek)
            Log.d(
                "ReminderAlarmScheduler",
                "cancel(): id=$nonNullId, dayOfWeek=$dayOfWeek"
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    companion object {
        private const val REQUEST_CODE_BASE = 1000
    }
}
