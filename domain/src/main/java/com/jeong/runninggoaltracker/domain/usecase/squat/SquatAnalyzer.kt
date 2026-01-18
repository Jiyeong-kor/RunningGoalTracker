package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_CALIBRATION_REQUIRED_FRAMES
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
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
    private val scorer: SquatFormScorer = SquatFormScorer(),
    private val qualityEvaluator: SquatQualityEvaluator = SquatQualityEvaluator(),
    private val sideSelector: SideSelector = SideSelector()
) : ExerciseAnalyzer {
    private var calibration: PoseCalibration? = null
    private var calibrationFrames: Int = SQUAT_INT_ZERO
    private var calibrationKneeSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationTrunkSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationLegSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationAnkleSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationKneeXSum: Float = SQUAT_FLOAT_ZERO
    private var repMinKneeAngle: Float? = null
    private var repMaxTrunkLean: Float? = null
    private var repMaxHeelRise: Float? = null
    private var repMaxKneeForward: Float? = null
    private var previousPhase: SquatPhase = SquatPhase.UP
    private var lastFeedbackType: PostureFeedbackType = PostureFeedbackType.UNKNOWN

    override fun analyze(frame: PoseFrame): PoseAnalysisResult {
        val sideSelection = sideSelector.update(frame, previousPhase)
        val metrics =
            sideSelection.selectedSide?.let { metricsCalculator.calculate(frame, calibration, it) }
        val counterResult = repCounter.update(frame.timestampMs, metrics)
        val skippedLowConfidence = metrics == null
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
                repSummary = null,
                skippedLowConfidence = skippedLowConfidence
            )
        }
        if (calibration == null && counterResult.isReliable && counterResult.phase == SquatPhase.UP) {
            metrics?.let { accumulateCalibration(it) }
        }
        val repSummary = handleRepTracking(counterResult, metrics)
        val feedbackType = when {
            !counterResult.isReliable -> PostureFeedbackType.NOT_IN_FRAME
            repSummary != null -> qualityEvaluator.feedbackType(repSummary)
                .also { lastFeedbackType = it }

            else -> lastFeedbackType
        }
        val accuracy = accuracyFor(counterResult.kneeAngleEma)
        val isPerfectForm = feedbackType == PostureFeedbackType.GOOD_FORM
        val side = sideSelection.selectedSide ?: PoseSide.LEFT
        val frameMetrics = SquatFrameMetrics(
            kneeAngleRaw = counterResult.kneeAngleRaw,
            kneeAngleEma = counterResult.kneeAngleEma,
            trunkLeanAngleRaw = counterResult.trunkLeanRaw,
            trunkLeanAngleEma = counterResult.trunkLeanEma,
            heelRiseRatio = metrics?.heelRiseRatio,
            kneeForwardRatio = metrics?.kneeForwardRatio,
            phase = counterResult.phase,
            side = side,
            isSideLocked = sideSelection.isLocked,
            upThreshold = repCounter.upThreshold(),
            downThreshold = repCounter.downThreshold(),
            upFramesRequired = repCounter.upFramesRequired(),
            downFramesRequired = repCounter.downFramesRequired(),
            upCandidateFrames = counterResult.upCandidateFrames,
            downCandidateFrames = counterResult.downCandidateFrames,
            leftConfidenceSum = sideSelection.leftConfidenceSum,
            rightConfidenceSum = sideSelection.rightConfidenceSum,
            rotationDegrees = frame.rotationDegrees,
            isFrontCamera = frame.isFrontCamera,
            isMirroringApplied = frame.isMirrored,
            transition = counterResult.transition,
            isLandmarkReliable = counterResult.isReliable,
            isCalibrated = calibration != null
        )
        previousPhase = counterResult.phase
        return PoseAnalysisResult(
            repCount = RepCount(counterResult.repCount, isIncremented = counterResult.repCompleted),
            feedback = PostureFeedback(
                type = feedbackType,
                isValid = repSummary != null,
                accuracy = accuracy,
                isPerfectForm = isPerfectForm
            ),
            frameMetrics = frameMetrics,
            repSummary = repSummary,
            skippedLowConfidence = skippedLowConfidence
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
        metrics: PoseRawSquatMetrics?
    ): SquatRepSummary? {
        if (counterResult.phase == SquatPhase.DOWN && previousPhase == SquatPhase.UP) {
            resetRepTracking()
        }
        if (counterResult.phase == SquatPhase.DOWN && metrics != null) {
            val kneeAngle = counterResult.kneeAngleEma
            val trunkLean = counterResult.trunkLeanEma
            repMinKneeAngle = repMinKneeAngle?.let { min(it, kneeAngle) } ?: kneeAngle
            repMaxTrunkLean = repMaxTrunkLean?.let { max(it, trunkLean) } ?: trunkLean
            val heel = metrics.heelRiseRatio
            if (heel != null) {
                repMaxHeelRise = repMaxHeelRise?.let { max(it, heel) } ?: heel
            }
            val kneeForward = metrics.kneeForwardRatio
            if (kneeForward != null) {
                repMaxKneeForward = repMaxKneeForward?.let { max(it, kneeForward) } ?: kneeForward
            }
        }
        if (counterResult.repCompleted) {
            val minKnee = repMinKneeAngle ?: counterResult.kneeAngleEma
            val maxTrunk = repMaxTrunkLean ?: counterResult.trunkLeanEma
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

    private fun accuracyFor(kneeAngle: Float): Float {
        val depthRange = SQUAT_STANDING_KNEE_ANGLE_THRESHOLD - SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
        val normalized = ((kneeAngle - SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD) / depthRange)
            .coerceIn(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_ONE)
        return (SQUAT_FLOAT_ONE - normalized).coerceIn(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_ONE)
    }
}
