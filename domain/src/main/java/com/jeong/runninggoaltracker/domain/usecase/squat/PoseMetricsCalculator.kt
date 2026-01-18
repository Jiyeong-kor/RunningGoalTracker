package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_MIN_LANDMARK_CONFIDENCE
import com.jeong.runninggoaltracker.domain.model.PoseCalibration
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PoseLandmark
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
import com.jeong.runninggoaltracker.domain.model.PoseSide
import kotlin.math.abs
import kotlin.math.sqrt

data class PoseRawSquatMetrics(
    val kneeAngle: Float,
    val trunkTiltVerticalAngle: Float,
    val trunkToThighAngle: Float,
    val heelRiseRatio: Float?,
    val kneeForwardRatio: Float?,
    val legLength: Float,
    val ankleY: Float,
    val kneeX: Float,
    val side: PoseSide
)

class PoseMetricsCalculator(
    private val minConfidence: Float = SQUAT_MIN_LANDMARK_CONFIDENCE,
    private val angleCalculator: AngleCalculator = AngleCalculator()
) {
    fun calculate(
        frame: PoseFrame,
        calibration: PoseCalibration?,
        side: PoseSide
    ): PoseRawSquatMetrics? {
        val landmarks = createSideLandmarks(frame, side) ?: return null
        val kneeAngle =
            angleCalculator.kneeAngle(landmarks.hip, landmarks.knee, landmarks.ankle) ?: return null
        val trunkTiltVerticalAngle =
            angleCalculator.trunkTiltVerticalAngle(landmarks.shoulder, landmarks.hip) ?: return null
        val trunkToThighAngle =
            angleCalculator.trunkToThighAngle(
                landmarks.shoulder,
                landmarks.hip,
                landmarks.knee
            ) ?: return null
        val legLength = distance(landmarks.hip, landmarks.ankle)
        val normalizedLength = calibration?.baselineLegLength ?: legLength
        val heelRiseRatio =
            calibration?.let { ratio(it.baselineAnkleY - landmarks.ankle.y, normalizedLength) }
        val kneeForwardRatio =
            calibration?.let { ratio(abs(landmarks.knee.x - landmarks.ankle.x), normalizedLength) }
        return PoseRawSquatMetrics(
            kneeAngle = kneeAngle,
            trunkTiltVerticalAngle = trunkTiltVerticalAngle,
            trunkToThighAngle = trunkToThighAngle,
            heelRiseRatio = heelRiseRatio,
            kneeForwardRatio = kneeForwardRatio,
            legLength = legLength,
            ankleY = landmarks.ankle.y,
            kneeX = landmarks.knee.x,
            side = side
        )
    }

    private fun createSideLandmarks(frame: PoseFrame, side: PoseSide): SideLandmarks? {
        val shoulder = if (side == PoseSide.LEFT) {
            frame.landmark(PoseLandmarkType.LEFT_SHOULDER)
        } else {
            frame.landmark(PoseLandmarkType.RIGHT_SHOULDER)
        }
        val hip = if (side == PoseSide.LEFT) {
            frame.landmark(PoseLandmarkType.LEFT_HIP)
        } else {
            frame.landmark(PoseLandmarkType.RIGHT_HIP)
        }
        val knee = if (side == PoseSide.LEFT) {
            frame.landmark(PoseLandmarkType.LEFT_KNEE)
        } else {
            frame.landmark(PoseLandmarkType.RIGHT_KNEE)
        }
        val ankle = if (side == PoseSide.LEFT) {
            frame.landmark(PoseLandmarkType.LEFT_ANKLE)
        } else {
            frame.landmark(PoseLandmarkType.RIGHT_ANKLE)
        }
        if (shoulder == null || hip == null || knee == null || ankle == null) return null
        val minConfidenceValue = listOf(shoulder, hip, knee, ankle).minOf { it.confidence }
        return if (minConfidenceValue >= minConfidence) {
            SideLandmarks(shoulder, hip, knee, ankle)
        } else {
            null
        }
    }

    private fun distance(first: PoseLandmark, last: PoseLandmark): Float {
        val dx = first.x - last.x
        val dy = first.y - last.y
        return sqrt(dx * dx + dy * dy)
    }

    private fun ratio(numerator: Float, denominator: Float): Float? =
        if (denominator == SQUAT_FLOAT_ZERO) null else numerator / denominator
}

data class SideLandmarks(
    val shoulder: PoseLandmark,
    val hip: PoseLandmark,
    val knee: PoseLandmark,
    val ankle: PoseLandmark
)
