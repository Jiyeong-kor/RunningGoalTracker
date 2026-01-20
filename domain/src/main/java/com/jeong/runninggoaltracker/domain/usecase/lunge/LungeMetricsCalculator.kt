package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_HALF
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_MIN_LANDMARK_CONFIDENCE
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PoseLandmark
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
import com.jeong.runninggoaltracker.domain.model.PoseSide
import com.jeong.runninggoaltracker.domain.usecase.squat.AngleCalculator
import kotlin.math.abs

class LungeMetricsCalculator(
    private val minConfidence: Float = LUNGE_MIN_LANDMARK_CONFIDENCE,
    private val angleCalculator: AngleCalculator = AngleCalculator()
) {
    fun calculate(frame: PoseFrame): LungeRawMetrics? {
        val leftShoulder = frame.landmark(PoseLandmarkType.LEFT_SHOULDER) ?: return null
        val rightShoulder = frame.landmark(PoseLandmarkType.RIGHT_SHOULDER) ?: return null
        val leftHip = frame.landmark(PoseLandmarkType.LEFT_HIP) ?: return null
        val rightHip = frame.landmark(PoseLandmarkType.RIGHT_HIP) ?: return null
        val leftKnee = frame.landmark(PoseLandmarkType.LEFT_KNEE) ?: return null
        val rightKnee = frame.landmark(PoseLandmarkType.RIGHT_KNEE) ?: return null
        val leftAnkle = frame.landmark(PoseLandmarkType.LEFT_ANKLE) ?: return null
        val rightAnkle = frame.landmark(PoseLandmarkType.RIGHT_ANKLE) ?: return null
        val minConfidenceValue =
            listOf(
                leftShoulder,
                rightShoulder,
                leftHip,
                rightHip,
                leftKnee,
                rightKnee,
                leftAnkle,
                rightAnkle
            ).minOf { it.confidence }
        if (minConfidenceValue < minConfidence) return null
        val leftKneeAngle = angleCalculator.kneeAngle(leftHip, leftKnee, leftAnkle) ?: return null
        val rightKneeAngle =
            angleCalculator.kneeAngle(rightHip, rightKnee, rightAnkle) ?: return null
        val leftTrunkToThighAngle =
            angleCalculator.trunkToThighAngle(leftShoulder, leftHip, leftKnee) ?: return null
        val rightTrunkToThighAngle =
            angleCalculator.trunkToThighAngle(rightShoulder, rightHip, rightKnee) ?: return null
        val midShoulder = midpoint(leftShoulder, rightShoulder)
        val midHip = midpoint(leftHip, rightHip)
        val trunkTiltVerticalAngle = angleCalculator.trunkTiltVerticalAngle(midShoulder, midHip)
            ?: return null
        val frameWidth = LUNGE_FLOAT_ONE
        val leftKneeForwardRatio = ratio(abs(leftKnee.x - leftAnkle.x), frameWidth)
        val rightKneeForwardRatio = ratio(abs(rightKnee.x - rightAnkle.x), frameWidth)
        val collapseRatios = run {
            val centerX = LUNGE_FLOAT_HALF
            val leftCollapse = collapseRatio(leftKnee, leftAnkle, centerX, frameWidth)
            val rightCollapse = collapseRatio(rightKnee, rightAnkle, centerX, frameWidth)
            Pair(leftCollapse, rightCollapse)
        }
        return LungeRawMetrics(
            leftKneeAngle = leftKneeAngle,
            rightKneeAngle = rightKneeAngle,
            trunkTiltVerticalAngle = trunkTiltVerticalAngle,
            leftTrunkToThighAngle = leftTrunkToThighAngle,
            rightTrunkToThighAngle = rightTrunkToThighAngle,
            leftKneeForwardRatio = leftKneeForwardRatio,
            rightKneeForwardRatio = rightKneeForwardRatio,
            leftKneeCollapseRatio = collapseRatios.first,
            rightKneeCollapseRatio = collapseRatios.second,
            hipCenterX = midHip.x,
            shoulderCenterX = midShoulder.x
        )
    }

    fun kneeAngle(frame: PoseFrame, side: PoseSide): Float? {
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
        if (hip == null || knee == null || ankle == null) return null
        val minConfidenceValue = listOf(hip, knee, ankle).minOf { it.confidence }
        if (minConfidenceValue < minConfidence) return null
        return angleCalculator.kneeAngle(hip, knee, ankle)
    }

    private fun midpoint(first: PoseLandmark, second: PoseLandmark): PoseLandmark =
        PoseLandmark(
            type = PoseLandmarkType.NOSE,
            x = (first.x + second.x) * LUNGE_FLOAT_HALF,
            y = (first.y + second.y) * LUNGE_FLOAT_HALF,
            z = (first.z + second.z) * LUNGE_FLOAT_HALF,
            confidence = (first.confidence + second.confidence) * LUNGE_FLOAT_HALF
        )

    private fun collapseRatio(
        knee: PoseLandmark,
        ankle: PoseLandmark,
        centerX: Float,
        frameWidth: Float
    ): Float? {
        val kneeDistance = abs(knee.x - centerX)
        val ankleDistance = abs(ankle.x - centerX)
        val inward = (ankleDistance - kneeDistance).coerceAtLeast(LUNGE_FLOAT_ZERO)
        return ratio(inward, frameWidth)
    }

    private fun ratio(numerator: Float, denominator: Float): Float? =
        if (denominator == LUNGE_FLOAT_ZERO) {
            null
        } else {
            (numerator / denominator).coerceAtLeast(LUNGE_FLOAT_ZERO)
        }
}

data class LungeRawMetrics(
    val leftKneeAngle: Float,
    val rightKneeAngle: Float,
    val trunkTiltVerticalAngle: Float,
    val leftTrunkToThighAngle: Float,
    val rightTrunkToThighAngle: Float,
    val leftKneeForwardRatio: Float?,
    val rightKneeForwardRatio: Float?,
    val leftKneeCollapseRatio: Float?,
    val rightKneeCollapseRatio: Float?,
    val hipCenterX: Float,
    val shoulderCenterX: Float
)
