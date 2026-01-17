package com.jeong.runninggoaltracker.shared.designsystem.config

import android.content.Context
import com.jeong.runninggoaltracker.shared.designsystem.R

object NumericResourceProvider {
    fun zeroInt(context: Context): Int {
        return context.resources.getInteger(R.integer.numeric_zero)
    }

    fun oneInt(context: Context): Int {
        return context.resources.getInteger(R.integer.numeric_one)
    }

    fun zeroLong(context: Context): Long {
        return zeroInt(context).toLong()
    }

    fun zeroDouble(context: Context): Double {
        return zeroInt(context).toDouble()
    }

    fun zeroFloat(context: Context): Float {
        return zeroInt(context).toFloat()
    }

    fun metersInKm(context: Context): Double {
        return context.resources.getInteger(R.integer.record_meters_in_km).toDouble()
    }

    fun updateIntervalMillis(context: Context): Long {
        return context.resources.getInteger(R.integer.record_update_interval_millis).toLong()
    }

    fun elapsedUpdateIntervalMillis(context: Context): Long {
        return context.resources.getInteger(R.integer.record_elapsed_update_interval_millis).toLong()
    }

    fun minDistanceMeters(context: Context): Float {
        return context.resources.getInteger(R.integer.record_min_distance_meters).toFloat()
    }

    fun recordNotificationId(context: Context): Int {
        return context.resources.getInteger(R.integer.record_notification_id)
    }

    fun recordStopRequestCode(context: Context): Int {
        return context.resources.getInteger(R.integer.record_stop_request_code)
    }

    fun activityRecognitionRequestCode(context: Context): Int {
        return context.resources.getInteger(R.integer.record_activity_recognition_request_code)
    }

    fun activityRecognitionIntervalMillis(context: Context): Long {
        return context.resources.getInteger(R.integer.record_activity_recognition_interval_millis).toLong()
    }

    fun reminderNotificationId(context: Context): Int {
        return context.resources.getInteger(R.integer.reminder_notification_id)
    }

    fun reminderRequestCodeBase(context: Context): Int {
        return context.resources.getInteger(R.integer.reminder_request_code_base)
    }

    fun reminderRequestCodeIdMultiplier(context: Context): Int {
        return context.resources.getInteger(R.integer.reminder_request_code_id_multiplier)
    }

    fun reminderRequestCodeHourMultiplier(context: Context): Int {
        return context.resources.getInteger(R.integer.reminder_request_code_hour_multiplier)
    }

    fun reminderRequestCodeMinuteMultiplier(context: Context): Int {
        return context.resources.getInteger(R.integer.reminder_request_code_minute_multiplier)
    }

    fun reminderDayOfWeekMin(context: Context): Int {
        return context.resources.getInteger(R.integer.reminder_day_of_week_min)
    }

    fun reminderDayOfWeekMax(context: Context): Int {
        return context.resources.getInteger(R.integer.reminder_day_of_week_max)
    }
}
