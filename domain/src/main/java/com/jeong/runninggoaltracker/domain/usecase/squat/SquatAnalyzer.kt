package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_CALIBRATION_REQUIRED_FRAMES
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_KNEE_FORWARD_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_MAX_TRUNK_LEAN_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_HEEL_RISE_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_SHALLOW_DEPTH_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_STANDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.model.PoseAnalysisResult
import com.jeong.runninggoaltracker.domain.model.PoseCalibration
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PostureFeedback
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.PoseSide
import com.jeong.runninggoaltracker.domain.model.RepCount
import com.jeong.runninggoaltracker.domain.model.SquatFrameMetrics
import com.jeong.runninggoaltracker.domain.model.SquatRepSummary
import com.jeong.runninggoaltracker.domain.model.SquatPhase
import com.jeong.runninggoaltracker.domain.usecase.ExerciseAnalyzer
import kotlin.math.max
import kotlin.math.min

class SquatAnalyzer(
    private val metricsCalculator: PoseMetricsCalculator = PoseMetricsCalculator(),
    private val repCounter: SquatRepCounter = SquatRepCounter(),
    private val scorer: SquatFormScorer = SquatFormScorer()
) : ExerciseAnalyzer {
    private var calibration: PoseCalibration? = null
    private var calibrationFrames: Int = SQUAT_INT_ZERO
    private var calibrationKneeSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationTrunkSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationLegSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationAnkleSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationKneeXSum: Float = SQUAT_FLOAT_ZERO
    private var lastMetrics: PoseRawSquatMetrics? = null
    private var repMinKneeAngle: Float? = null
    private var repMaxTrunkLean: Float? = null
    private var repMaxHeelRise: Float? = null
    private var repMaxKneeForward: Float? = null
    private var previousPhase: SquatPhase = SquatPhase.UP

    override fun analyze(frame: PoseFrame): PoseAnalysisResult {
        val rawMetrics = metricsCalculator.calculate(frame, calibration)
        val isReliable = rawMetrics != null
        val metrics = rawMetrics ?: lastMetrics
        if (rawMetrics != null) {
            lastMetrics = rawMetrics
        }
        val counterResult = repCounter.update(frame.timestampMs, rawMetrics, isReliable)
        if (counterResult == null) {
            return PoseAnalysisResult(
                repCount = RepCount(SQUAT_INT_ZERO, isIncremented = false),
                feedback = PostureFeedback(
                    type = PostureFeedbackType.UNKNOWN,
                    isValid = false,
                    accuracy = SQUAT_FLOAT_ZERO,
                    isPerfectForm = false
                ),
                frameMetrics = null,
                repSummary = null
            )
        }
        if (calibration == null && isReliable && counterResult.phase == SquatPhase.UP) {
            accumulateCalibration(rawMetrics)
        }
        val repSummary = handleRepTracking(
            counterResult,
            metrics,
            counterResult.kneeAngle,
            counterResult.trunkLeanAngle
        )
        previousPhase = counterResult.phase
        val feedbackType = feedbackTypeFor(
            counterResult,
            metrics,
            counterResult.kneeAngle,
            counterResult.trunkLeanAngle
        )
        val accuracy = accuracyFor(counterResult.kneeAngle)
        val isPerfectForm = feedbackType == PostureFeedbackType.GOOD_FORM
        val side = metrics?.side ?: lastMetrics?.side ?: PoseSide.LEFT
        val frameMetrics = SquatFrameMetrics(
            kneeAngle = counterResult.kneeAngle,
            trunkLeanAngle = counterResult.trunkLeanAngle,
            heelRiseRatio = metrics?.heelRiseRatio,
            kneeForwardRatio = metrics?.kneeForwardRatio,
            phase = counterResult.phase,
            side = side,
            upThreshold = repCounter.upThreshold(),
            downThreshold = repCounter.downThreshold(),
            upFramesRequired = repCounter.upFramesRequired(),
            downFramesRequired = repCounter.downFramesRequired(),
            transition = counterResult.transition,
            isLandmarkReliable = counterResult.isReliable,
            isCalibrated = calibration != null
        )
        return PoseAnalysisResult(
            repCount = RepCount(counterResult.repCount, isIncremented = counterResult.repCompleted),
            feedback = PostureFeedback(
                type = feedbackType,
                isValid = feedbackType != PostureFeedbackType.TOO_SHALLOW &&
                        feedbackType != PostureFeedbackType.NOT_IN_FRAME,
                accuracy = accuracy,
                isPerfectForm = isPerfectForm
            ),
            frameMetrics = frameMetrics,
            repSummary = repSummary
        )
    }

    private fun accumulateCalibration(metrics: PoseRawSquatMetrics) {
        calibrationFrames += SQUAT_INT_ONE
        calibrationKneeSum += metrics.kneeAngle
        calibrationTrunkSum += metrics.trunkLeanAngle
        calibrationLegSum += metrics.legLength
        calibrationAnkleSum += metrics.ankleY
        calibrationKneeXSum += metrics.kneeX
        if (calibrationFrames >= SQUAT_CALIBRATION_REQUIRED_FRAMES) {
            val divisor = calibrationFrames.toFloat()
            calibration = PoseCalibration(
                baselineKneeAngle = calibrationKneeSum / divisor,
                baselineTrunkLeanAngle = calibrationTrunkSum / divisor,
                baselineLegLength = calibrationLegSum / divisor,
                baselineAnkleY = calibrationAnkleSum / divisor,
                baselineKneeX = calibrationKneeXSum / divisor
            )
        }
    }

    private fun handleRepTracking(
        counterResult: SquatRepCounterResult,
        metrics: PoseRawSquatMetrics?,
        kneeAngle: Float,
        trunkLean: Float
    ): SquatRepSummary? {
        if (counterResult.phase == SquatPhase.DOWN && previousPhase == SquatPhase.UP) {
            resetRepTracking()
        }
        if (counterResult.phase == SquatPhase.DOWN) {
            repMinKneeAngle = repMinKneeAngle?.let { min(it, kneeAngle) } ?: kneeAngle
            repMaxTrunkLean = repMaxTrunkLean?.let { max(it, trunkLean) } ?: trunkLean
            val heel = metrics?.heelRiseRatio
            if (heel != null) {
                repMaxHeelRise = repMaxHeelRise?.let { max(it, heel) } ?: heel
            }
            val kneeForward = metrics?.kneeForwardRatio
            if (kneeForward != null) {
                repMaxKneeForward = repMaxKneeForward?.let { max(it, kneeForward) } ?: kneeForward
            }
        }
        if (counterResult.repCompleted) {
            val minKnee = repMinKneeAngle ?: kneeAngle
            val maxTrunk = repMaxTrunkLean ?: trunkLean
            val summary = scorer.score(
                SquatRepMetrics(
                    minKneeAngle = minKnee,
                    maxTrunkLeanAngle = maxTrunk,
                    maxHeelRiseRatio = repMaxHeelRise,
                    maxKneeForwardRatio = repMaxKneeForward
                )
            )
            resetRepTracking()
            return summary
        }
        return null
    }

    private fun resetRepTracking() {
        repMinKneeAngle = null
        repMaxTrunkLean = null
        repMaxHeelRise = null
        repMaxKneeForward = null
    }

    private fun feedbackTypeFor(
        counterResult: SquatRepCounterResult,
        metrics: PoseRawSquatMetrics?,
        kneeAngle: Float,
        trunkLean: Float
    ): PostureFeedbackType {
        if (!counterResult.isReliable) return PostureFeedbackType.NOT_IN_FRAME
        val heelRiseRatio = metrics?.heelRiseRatio
        if (heelRiseRatio != null && heelRiseRatio > SQUAT_HEEL_RISE_RATIO_THRESHOLD) {
            return PostureFeedbackType.HEEL_RISE
        }
        val kneeForwardRatio = metrics?.kneeForwardRatio
        if (kneeForwardRatio != null && kneeForwardRatio > SQUAT_KNEE_FORWARD_RATIO_THRESHOLD) {
            return PostureFeedbackType.KNEE_FORWARD
        }
        if (trunkLean > SQUAT_MAX_TRUNK_LEAN_ANGLE_THRESHOLD) {
            return PostureFeedbackType.EXCESS_FORWARD_LEAN
        }
        val minKnee = repMinKneeAngle ?: kneeAngle
        if (counterResult.phase == SquatPhase.DOWN && minKnee > SQUAT_SHALLOW_DEPTH_ANGLE_THRESHOLD) {
            return PostureFeedbackType.TOO_SHALLOW
        }
        return if (kneeAngle <= SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD &&
            trunkLean <= SQUAT_MAX_TRUNK_LEAN_ANGLE_THRESHOLD
        ) {
            PostureFeedbackType.GOOD_FORM
        } else {
            PostureFeedbackType.STAND_TALL
        }
    }

    private fun accuracyFor(kneeAngle: Float): Float {
        val depthRange = SQUAT_STANDING_KNEE_ANGLE_THRESHOLD - SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
        val normalized = ((kneeAngle - SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD) / depthRange)
            .coerceIn(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_ONE)
        return (SQUAT_FLOAT_ONE - normalized).coerceIn(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_ONE)
    }
}
