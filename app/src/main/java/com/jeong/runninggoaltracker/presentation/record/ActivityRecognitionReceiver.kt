package com.jeong.runninggoaltracker.presentation.record

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import kotlin.collections.ArrayDeque

class ActivityRecognitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (!ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionStateHolder.update("NO_RESULT")
            return
        }

        val result = ActivityRecognitionResult.extractResult(intent) ?: run {
            ActivityRecognitionStateHolder.update("NO_RESULT")
            return
        }

        val activities: List<DetectedActivity> = result.probableActivities
        if (activities.isEmpty()) {
            ActivityRecognitionStateHolder.update("NO_ACTIVITY")
            return
        }

        val rawLabel = getReadableActivity(activities)

        val labelForSmoothing =
            if (rawLabel == "UNKNOWN") {
                ActivityRecognitionStateHolder.state.value.label
            } else {
                rawLabel
            }

        val smoothLabel = ActivitySmoother.push(labelForSmoothing)

        Log.d(
            "ActivityRecognition",
            "raw=$rawLabel, smooth=$smoothLabel, all=$activities"
        )

        ActivityRecognitionStateHolder.update(
            label = smoothLabel
        )

        ActivityLogHolder.add(
            label = smoothLabel
        )
    }

    private fun getReadableActivity(
        activities: List<DetectedActivity>
    ): String {

        val types: Set<Int> = activities.map { it.type }.toSet()

        return when {
            DetectedActivity.RUNNING in types -> "RUNNING"
            DetectedActivity.WALKING in types ||
                    DetectedActivity.ON_FOOT in types -> "WALKING"

            DetectedActivity.ON_BICYCLE in types -> "ON_BICYCLE"
            DetectedActivity.IN_VEHICLE in types -> "IN_VEHICLE"
            DetectedActivity.STILL in types -> "STILL"
            else -> "UNKNOWN"
        }
    }
}

private object ActivitySmoother {

    private const val WINDOW_SIZE = 3
    private val buffer: ArrayDeque<String> = ArrayDeque()

    fun push(label: String): String {
        buffer.addLast(label)
        if (buffer.size > WINDOW_SIZE) {
            buffer.removeFirst()
        }

        val counted = buffer.groupingBy { it }.eachCount()
        return counted.maxByOrNull { it.value }?.key ?: label
    }
}
