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
            ActivityRecognitionStateHolder.update("NO_RESULT", 0)
            return
        }

        val result = ActivityRecognitionResult.extractResult(intent) ?: run {
            ActivityRecognitionStateHolder.update("NO_RESULT", 0)
            return
        }

        val activities: List<DetectedActivity> = result.probableActivities
        if (activities.isEmpty()) {
            ActivityRecognitionStateHolder.update("NO_ACTIVITY", 0)
            return
        }

        // 1) Google 결과를 우리가 원하는 기준으로 해석
        val (rawLabel, confidence) = getReadableActivity(activities)

        // 2) 낮은 신뢰도의 UNKNOWN 은 무시하고 기존 상태 유지
        val labelForSmoothing = if (rawLabel == "UNKNOWN" && confidence < 70) {
            ActivityRecognitionStateHolder.state.value.label
        } else {
            rawLabel
        }

        // 3) 최근 여러 번의 결과를 스무딩
        val smoothLabel = ActivitySmoother.push(labelForSmoothing)

        Log.d(
            "ActivityRecognition",
            "raw=$rawLabel, smooth=$smoothLabel, conf=$confidence, all=$activities"
        )

        ActivityRecognitionStateHolder.update(
            label = smoothLabel,
            confidence = confidence
        )

        ActivityLogHolder.add(
            label = smoothLabel,
            confidence = confidence
        )
    }

    /**
     * probableActivities 전체를 보고 우리가 원하는 기준으로 라벨 + confidence를 뽑는다.
     */
    private fun getReadableActivity(
        activities: List<DetectedActivity>
    ): Pair<String, Int> {

        val byType: Map<Int, Int> = activities.associate { it.type to it.confidence }

        val walkingConf = byType[DetectedActivity.WALKING] ?: 0
        val runningConf = byType[DetectedActivity.RUNNING] ?: 0
        val onFootConf = byType[DetectedActivity.ON_FOOT] ?: 0
        val stillConf = byType[DetectedActivity.STILL] ?: 0
        val inVehicleConf = byType[DetectedActivity.IN_VEHICLE] ?: 0
        val onBicycleConf = byType[DetectedActivity.ON_BICYCLE] ?: 0
        val tiltingConf = byType[DetectedActivity.TILTING] ?: 0
        val unknownConf = byType[DetectedActivity.UNKNOWN] ?: 0

        val walkingCombined = maxOf(walkingConf, onFootConf)

        // 1) 걷기/뛰기 우선: 값만 있으면 사용
        if (runningConf > 0 || walkingCombined > 0) {
            return if (runningConf >= walkingCombined) {
                "RUNNING" to runningConf
            } else {
                "WALKING" to walkingCombined
            }
        }

        // 2) 가만히
        if (stillConf > 0) {
            return "STILL" to stillConf
        }

        // 3) 차량/자전거
        if (inVehicleConf > 0) {
            return "IN_VEHICLE" to inVehicleConf
        }
        if (onBicycleConf > 0) {
            return "ON_BICYCLE" to onBicycleConf
        }

        // 4) non-UNKNOWN 중 가장 높은 값 사용 (TILTING은 제외)
        val bestNonUnknown = activities
            .filter {
                it.type != DetectedActivity.UNKNOWN &&
                        it.type != DetectedActivity.TILTING   // ← 여기에서 TILTING 제외
            }
            .maxByOrNull { it.confidence }

        if (bestNonUnknown != null) {
            val bestLabel = when (bestNonUnknown.type) {
                DetectedActivity.RUNNING -> "RUNNING"
                DetectedActivity.WALKING,
                DetectedActivity.ON_FOOT -> "WALKING"
                DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
                DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
                DetectedActivity.STILL -> "STILL"
                else -> "UNKNOWN"
            }
            return bestLabel to bestNonUnknown.confidence
        }

        // 5) 전부 UNKNOWN/TILTING밖에 없다면 UNKNOWN
        return "UNKNOWN" to maxOf(unknownConf, tiltingConf)
    }
}

/**
 * 최근 N개의 라벨을 모아서 다수결로 스무딩.
 */
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
