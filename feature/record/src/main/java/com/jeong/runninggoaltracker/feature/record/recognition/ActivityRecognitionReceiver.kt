package com.jeong.runninggoaltracker.feature.record.recognition

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class ActivityRecognitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (!ActivityRecognitionResult.hasResult(intent)) {
            getStateUpdater(context).update(ActivityRecognitionStatus.NoResult)
            return
        }

        val result = ActivityRecognitionResult.extractResult(intent) ?: run {
            getStateUpdater(context).update(ActivityRecognitionStatus.NoResult)
            return
        }

        val activities: List<DetectedActivity> = result.probableActivities
        if (activities.isEmpty()) {
            getStateUpdater(context).update(ActivityRecognitionStatus.NoActivity)
            return
        }

        val rawStatus = getReadableActivity(activities)

        val statusForSmoothing =
            if (rawStatus == ActivityRecognitionStatus.Unknown) {
                getStateHolder(context).state.value.status
            } else {
                rawStatus
            }

        val smoothStatus = ActivitySmoother.push(statusForSmoothing)

        getStateUpdater(context).update(
            status = smoothStatus
        )

        ActivityLogHolder.add(
            status = smoothStatus
        )
    }

    private fun getReadableActivity(
        activities: List<DetectedActivity>
    ): ActivityRecognitionStatus {

        val types: Set<Int> = activities.map { it.type }.toSet()

        return when {
            DetectedActivity.RUNNING in types -> ActivityRecognitionStatus.Running
            DetectedActivity.WALKING in types ||
                    DetectedActivity.ON_FOOT in types -> ActivityRecognitionStatus.Walking

            DetectedActivity.ON_BICYCLE in types -> ActivityRecognitionStatus.OnBicycle
            DetectedActivity.IN_VEHICLE in types -> ActivityRecognitionStatus.InVehicle
            DetectedActivity.STILL in types -> ActivityRecognitionStatus.Still
            else -> ActivityRecognitionStatus.Unknown
        }
    }

    private fun getStateHolder(context: Context): ActivityRecognitionStateHolder {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ActivityRecognitionEntryPoint::class.java
        )
        return entryPoint.activityRecognitionStateHolder()
    }

    private fun getStateUpdater(context: Context): ActivityStateUpdater = getStateHolder(context)

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(SingletonComponent::class)
    interface ActivityRecognitionEntryPoint {
        fun activityRecognitionStateHolder(): ActivityRecognitionStateHolder
    }
}

private object ActivitySmoother {

    private const val WINDOW_SIZE = 3
    private val buffer: ArrayDeque<ActivityRecognitionStatus> = ArrayDeque()

    fun push(status: ActivityRecognitionStatus): ActivityRecognitionStatus {
        buffer.addLast(status)
        if (buffer.size > WINDOW_SIZE) {
            buffer.removeFirst()
        }

        val counted = buffer.groupingBy { it }.eachCount()
        return counted.maxByOrNull { it.value }?.key ?: status
    }
}
