package com.jeong.runninggoaltracker.feature.reminder.alarm

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import com.jeong.runninggoaltracker.feature.reminder.contract.ReminderAlarmContract
import com.jeong.runninggoaltracker.shared.designsystem.config.NumericResourceProvider
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

    private fun getUniqueRequestCode(id: Int, hour: Int, minute: Int, dayOfWeek: Int): Int {
        val base = NumericResourceProvider.reminderRequestCodeBase(context)
        val idMultiplier = NumericResourceProvider.reminderRequestCodeIdMultiplier(context)
        val hourMultiplier = NumericResourceProvider.reminderRequestCodeHourMultiplier(context)
        val minuteMultiplier = NumericResourceProvider.reminderRequestCodeMinuteMultiplier(context)

        return base + id * idMultiplier + hour * hourMultiplier + minute * minuteMultiplier + dayOfWeek
    }

    private fun createPendingIntent(
        id: Int,
        hour: Int,
        minute: Int,
        dayOfWeek: Int
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = reminderAction(id, dayOfWeek)

            putExtra(ReminderAlarmContract.EXTRA_ID, id)
            putExtra(ReminderAlarmContract.EXTRA_HOUR, hour)
            putExtra(ReminderAlarmContract.EXTRA_MINUTE, minute)
            putExtra(ReminderAlarmContract.EXTRA_DAY_OF_WEEK, dayOfWeek)
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
            if (!canExact) {
                return
            }
        }

        val zeroInt = NumericResourceProvider.zeroInt(context)
        val oneInt = NumericResourceProvider.oneInt(context)

        days.forEach { dayOfWeek ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, zeroInt)
                set(Calendar.MILLISECOND, zeroInt)
                set(Calendar.DAY_OF_WEEK, dayOfWeek)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, oneInt)
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
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun reminderAction(id: Int, dayOfWeek: Int): String =
        ReminderAlarmContract.ACTION_REMINDER_ALARM_FORMAT.format(id, dayOfWeek)
}
