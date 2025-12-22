package com.jeong.runninggoaltracker.feature.reminder.alarm

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.feature.reminder.notification.ReminderNotifier
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.DayOfWeek

class ReminderAlarmReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @SuppressLint("ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val payload = intent.toAlarmPayload()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.hasPostNotificationsPermission()) {
            Log.d(TAG, "POST_NOTIFICATIONS 권한이 허용되지 않아 알림을 건너뜁니다")
            return
        }

        ReminderNotifier.showNow(context, payload.hour, payload.minute)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val reminder = intent.toRunningReminderOrNull()
        if (reminder == null) {
            Log.d(
                TAG,
                "유효하지 않은 데이터로 인해 알람 재예약을 건너뜁니다: id=${payload.id}, dayOfWeek=${payload.dayOfWeekRaw}"
            )
            return
        }

        getEntryPoint(context)
            .reminderScheduler()
            .scheduleIfNeeded(reminder)
    }

    private fun getEntryPoint(context: Context): ReminderAlarmReceiverEntryPoint =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderAlarmReceiverEntryPoint::class.java
        )

    private companion object {
        const val TAG = "ReminderAlarmReceiver"
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface ReminderAlarmReceiverEntryPoint {
    fun reminderScheduler(): ReminderScheduler
}

private data class AlarmPayload(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val dayOfWeekRaw: Int,
)

private fun Intent.toAlarmPayload() = AlarmPayload(
    id = getIntExtra(EXTRA_ID, 0),
    hour = getIntExtra(EXTRA_HOUR, 0),
    minute = getIntExtra(EXTRA_MINUTE, 0),
    dayOfWeekRaw = getIntExtra(EXTRA_DAY_OF_WEEK, 0),
)

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Context.hasPostNotificationsPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

@RequiresApi(Build.VERSION_CODES.O)
private fun Intent.toRunningReminderOrNull(): RunningReminder? {
    val id = getIntExtra(EXTRA_ID, 0).takeIf { it != 0 } ?: return null

    val dayOfWeek = runCatching {
        DayOfWeek.of(getIntExtra(EXTRA_DAY_OF_WEEK, 0))
    }.getOrNull() ?: return null

    val hour = getIntExtra(EXTRA_HOUR, 0)
    val minute = getIntExtra(EXTRA_MINUTE, 0)

    return RunningReminder(
        id = id,
        hour = hour,
        minute = minute,
        enabled = true,
        days = setOf(dayOfWeek),
    )
}

private const val EXTRA_ID = "id"
private const val EXTRA_HOUR = "hour"
private const val EXTRA_MINUTE = "minute"
private const val EXTRA_DAY_OF_WEEK = "dayOfWeek"
