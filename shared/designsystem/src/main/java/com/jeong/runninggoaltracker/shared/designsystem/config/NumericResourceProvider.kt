package com.jeong.runninggoaltracker.shared.designsystem.config

import android.content.Context
import com.jeong.runninggoaltracker.shared.designsystem.R

object NumericResourceProvider {
    fun zeroInt(context: Context): Int = context.resources.getInteger(R.integer.numeric_zero)

    fun oneInt(context: Context): Int = context.resources.getInteger(R.integer.numeric_one)

    fun zeroLong(context: Context): Long = zeroInt(context).toLong()

    fun zeroDouble(context: Context): Double = zeroInt(context).toDouble()

    fun metersInKm(context: Context): Double =
        context.resources.getInteger(R.integer.record_meters_in_km).toDouble()

    fun updateIntervalMillis(context: Context): Long =
        context.resources.getInteger(R.integer.record_update_interval_millis).toLong()

    fun elapsedUpdateIntervalMillis(context: Context): Long =
        context.resources.getInteger(R.integer.record_elapsed_update_interval_millis).toLong()

    fun minDistanceMeters(context: Context): Float =
        context.resources.getInteger(R.integer.record_min_distance_meters).toFloat()

    fun recordNotificationId(context: Context): Int =
        context.resources.getInteger(R.integer.record_notification_id)

    fun recordStopRequestCode(context: Context): Int =
        context.resources.getInteger(R.integer.record_stop_request_code)

    fun activityRecognitionRequestCode(context: Context): Int =
        context.resources.getInteger(R.integer.record_activity_recognition_request_code)

    fun activityRecognitionIntervalMillis(context: Context): Long =
        context.resources.getInteger(R.integer.record_activity_recognition_interval_millis).toLong()

    fun reminderNotificationId(context: Context): Int =
        context.resources.getInteger(R.integer.reminder_notification_id)

    fun reminderRequestCodeBase(context: Context): Int =
        context.resources.getInteger(R.integer.reminder_request_code_base)

    fun reminderRequestCodeIdMultiplier(context: Context): Int =
        context.resources.getInteger(R.integer.reminder_request_code_id_multiplier)

    fun reminderRequestCodeHourMultiplier(context: Context): Int =
        context.resources.getInteger(R.integer.reminder_request_code_hour_multiplier)

    fun reminderRequestCodeMinuteMultiplier(context: Context): Int =
        context.resources.getInteger(R.integer.reminder_request_code_minute_multiplier)

    fun reminderDayOfWeekMin(context: Context): Int =
        context.resources.getInteger(R.integer.reminder_day_of_week_min)

    fun reminderDayOfWeekMax(context: Context): Int =
        context.resources.getInteger(R.integer.reminder_day_of_week_max)

    fun distanceFractionDigits(context: Context): Int =
        context.resources.getInteger(R.integer.distance_fraction_digits)

    fun percentageFractionDigits(context: Context): Int =
        context.resources.getInteger(R.integer.percentage_fraction_digits)

    fun percentageScale(context: Context): Int =
        context.resources.getInteger(R.integer.percentage_scale)

    fun throttleClickIntervalMillis(context: Context): Long =
        context.resources.getInteger(R.integer.throttle_click_interval_millis).toLong()
}
