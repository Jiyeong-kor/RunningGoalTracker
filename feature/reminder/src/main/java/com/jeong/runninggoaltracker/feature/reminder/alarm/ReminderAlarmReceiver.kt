package com.jeong.runninggoaltracker.feature.reminder.alarm

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.feature.reminder.contract.ReminderAlarmContract
import com.jeong.runninggoaltracker.feature.reminder.notification.ReminderNotifier
import com.jeong.runninggoaltracker.shared.designsystem.config.NumericResourceProvider
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val payload = intent.toAlarmPayload(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.hasPostNotificationsPermission()) {
            return
        }

        ReminderNotifier.showNow(context, payload.hour, payload.minute)

        val reminder = intent.toRunningReminderOrNull(context) ?: return

        getEntryPoint(context)
            .reminderScheduler()
            .scheduleIfNeeded(reminder)
    }

    private fun getEntryPoint(context: Context): ReminderAlarmReceiverEntryPoint =
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReminderAlarmReceiverEntryPoint::class.java
        )
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

private fun Intent.toAlarmPayload(context: Context): AlarmPayload {
    val zeroInt = NumericResourceProvider.zeroInt(context)

    return AlarmPayload(
        id = getIntExtra(ReminderAlarmContract.EXTRA_ID, zeroInt),
        hour = getIntExtra(ReminderAlarmContract.EXTRA_HOUR, zeroInt),
        minute = getIntExtra(ReminderAlarmContract.EXTRA_MINUTE, zeroInt),
        dayOfWeekRaw = getIntExtra(ReminderAlarmContract.EXTRA_DAY_OF_WEEK, zeroInt)
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private fun Context.hasPostNotificationsPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED

private fun Intent.toRunningReminderOrNull(context: Context): RunningReminder? {
    val zeroInt = NumericResourceProvider.zeroInt(context)
    val minDay = NumericResourceProvider.reminderDayOfWeekMin(context)
    val maxDay = NumericResourceProvider.reminderDayOfWeekMax(context)

    val id = getIntExtra(ReminderAlarmContract.EXTRA_ID, zeroInt)
        .takeIf { it != zeroInt }
        ?: return null

    val dayOfWeekRaw = getIntExtra(ReminderAlarmContract.EXTRA_DAY_OF_WEEK, zeroInt)
    if (dayOfWeekRaw !in minDay..maxDay) return null

    val hour = getIntExtra(ReminderAlarmContract.EXTRA_HOUR, zeroInt)
    val minute = getIntExtra(ReminderAlarmContract.EXTRA_MINUTE, zeroInt)

    return RunningReminder(
        id = id,
        hour = hour,
        minute = minute,
        enabled = true,
        days = setOf(dayOfWeekRaw)
    )
}
