package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_CALIBRATION_REQUIRED_FRAMES
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_HEEL_RISE_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_KNEE_FORWARD_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_SHALLOW_DEPTH_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_STANDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_TRUNK_TILT_VERTICAL_DIAGNOSTIC_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_TRUNK_TO_THIGH_ANGLE_HARD_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_TRUNK_TO_THIGH_ANGLE_SOFT_THRESHOLD
import com.jeong.runninggoaltracker.domain.model.ComparisonOperator
import com.jeong.runninggoaltracker.domain.model.PoseAnalysisResult
import com.jeong.runninggoaltracker.domain.model.PoseCalibration
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PostureFeedback
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.PostureWarningEvent
import com.jeong.runninggoaltracker.domain.model.PoseSide
import com.jeong.runninggoaltracker.domain.model.RepCount
import com.jeong.runninggoaltracker.domain.model.SquatWarningMetric
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
    private var calibrationTrunkTiltSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationLegSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationAnkleSum: Float = SQUAT_FLOAT_ZERO
    private var calibrationKneeXSum: Float = SQUAT_FLOAT_ZERO
    private var repMinKneeAngle: Float? = null
    private var repMinTrunkToThighAngle: Float? = null
    private var repMaxTrunkTiltVerticalAngle: Float? = null
    private var repMaxHeelRise: Float? = null
    private var repMaxKneeForward: Float? = null
    private var hasBottomFeedback: Boolean = false
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
                warningEvent = null,
                skippedLowConfidence = skippedLowConfidence
            )
        }
        if (calibration == null && counterResult.isReliable && counterResult.phase == SquatPhase.UP) {
            metrics?.let { accumulateCalibration(it) }
        }
        val feedbackResult = handleRepTracking(counterResult, metrics)
        val feedbackSummary = feedbackResult.repSummary ?: feedbackResult.bottomSummary
        val rawFeedbackType = when {
            !counterResult.isReliable -> PostureFeedbackType.NOT_IN_FRAME
            feedbackSummary != null -> qualityEvaluator.feedbackType(feedbackSummary)
                .also { lastFeedbackType = it }

            else -> lastFeedbackType
        }
        val feedbackType = suppressStandingFeedback(counterResult, rawFeedbackType)
        if (feedbackType != rawFeedbackType) {
            lastFeedbackType = feedbackType
        }
        val accuracy = accuracyFor(counterResult.kneeAngleEma)
        val isPerfectForm = feedbackType == PostureFeedbackType.GOOD_FORM
        val side = sideSelection.selectedSide ?: PoseSide.LEFT
        val isCameraTiltSuspected =
            counterResult.trunkTiltVerticalEma > SQUAT_TRUNK_TILT_VERTICAL_DIAGNOSTIC_THRESHOLD &&
                    counterResult.trunkToThighEma >= SQUAT_TRUNK_TO_THIGH_ANGLE_SOFT_THRESHOLD
        val repMinKnee = repMinKneeAngle ?: counterResult.kneeAngleEma
        val repMinTrunkToThigh = repMinTrunkToThighAngle ?: counterResult.trunkToThighEma
        val repMaxTrunkTilt = repMaxTrunkTiltVerticalAngle ?: counterResult.trunkTiltVerticalEma
        val frameMetrics = SquatFrameMetrics(
            kneeAngleRaw = counterResult.kneeAngleRaw,
            kneeAngleEma = counterResult.kneeAngleEma,
            trunkTiltVerticalAngleRaw = counterResult.trunkTiltVerticalRaw,
            trunkTiltVerticalAngleEma = counterResult.trunkTiltVerticalEma,
            trunkToThighAngleRaw = counterResult.trunkToThighRaw,
            trunkToThighAngleEma = counterResult.trunkToThighEma,
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
            repMinKneeAngle = repMinKnee,
            repMinTrunkToThighAngle = repMinTrunkToThigh,
            repMaxTrunkTiltVerticalAngle = repMaxTrunkTilt,
            leftConfidenceSum = sideSelection.leftConfidenceSum,
            rightConfidenceSum = sideSelection.rightConfidenceSum,
            rotationDegrees = frame.rotationDegrees,
            isFrontCamera = frame.isFrontCamera,
            isMirroringApplied = frame.isMirrored,
            isCameraTiltSuspected = isCameraTiltSuspected,
            transition = counterResult.transition,
            isLandmarkReliable = counterResult.isReliable,
            isCalibrated = calibration != null
        )
        val warningEvent = feedbackSummary?.let { summary ->
            if (feedbackType == rawFeedbackType && isFormCorrection(feedbackType)) {
                warningEventFor(feedbackType, summary, counterResult.phase, frame.timestampMs)
            } else {
                null
            }
        }
        previousPhase = counterResult.phase
        return PoseAnalysisResult(
            repCount = RepCount(counterResult.repCount, isIncremented = counterResult.repCompleted),
            feedback = PostureFeedback(
                type = feedbackType,
                isValid = feedbackSummary != null,
                accuracy = accuracy,
                isPerfectForm = isPerfectForm
            ),
            frameMetrics = frameMetrics,
            repSummary = feedbackResult.repSummary,
            warningEvent = warningEvent,
            skippedLowConfidence = skippedLowConfidence
        )
    }

    private fun accumulateCalibration(metrics: PoseRawSquatMetrics) {
        calibrationFrames += SQUAT_INT_ONE
        calibrationKneeSum += metrics.kneeAngle
        calibrationTrunkTiltSum += metrics.trunkTiltVerticalAngle
        calibrationLegSum += metrics.legLength
        calibrationAnkleSum += metrics.ankleY
        calibrationKneeXSum += metrics.kneeX
        if (calibrationFrames >= SQUAT_CALIBRATION_REQUIRED_FRAMES) {
            val divisor = calibrationFrames.toFloat()
            calibration = PoseCalibration(
                baselineKneeAngle = calibrationKneeSum / divisor,
                baselineTrunkTiltVerticalAngle = calibrationTrunkTiltSum / divisor,
                baselineLegLength = calibrationLegSum / divisor,
                baselineAnkleY = calibrationAnkleSum / divisor,
                baselineKneeX = calibrationKneeXSum / divisor
            )
        }
    }

    private fun handleRepTracking(
        counterResult: SquatRepCounterResult,
        metrics: PoseRawSquatMetrics?
    ): SquatFeedbackResult {
        var bottomSummary: SquatRepSummary? = null
        var repSummary: SquatRepSummary? = null
        if (counterResult.phase == SquatPhase.DOWN && previousPhase == SquatPhase.UP) {
            resetRepTracking()
            hasBottomFeedback = false
        }
        if (counterResult.phase == SquatPhase.DOWN && metrics != null) {
            val kneeAngle = counterResult.kneeAngleEma
            repMinKneeAngle = repMinKneeAngle?.let { min(it, kneeAngle) } ?: kneeAngle
            val trunkToThigh = counterResult.trunkToThighEma
            repMinTrunkToThighAngle =
                repMinTrunkToThighAngle?.let { min(it, trunkToThigh) } ?: trunkToThigh
            val trunkTilt = counterResult.trunkTiltVerticalEma
            repMaxTrunkTiltVerticalAngle =
                repMaxTrunkTiltVerticalAngle?.let { max(it, trunkTilt) } ?: trunkTilt
            val heel = metrics.heelRiseRatio
            if (heel != null) {
                repMaxHeelRise = repMaxHeelRise?.let { max(it, heel) } ?: heel
            }
            val kneeForward = metrics.kneeForwardRatio
            if (kneeForward != null) {
                repMaxKneeForward = repMaxKneeForward?.let { max(it, kneeForward) } ?: kneeForward
            }
        }
        if (counterResult.phase == SquatPhase.DOWN &&
            counterResult.kneeAngleEma <= repCounter.downThreshold() &&
            !hasBottomFeedback
        ) {
            bottomSummary = scorer.score(buildRepMetrics(counterResult))
            hasBottomFeedback = true
        }
        if (counterResult.repCompleted) {
            repSummary = scorer.score(buildRepMetrics(counterResult))
            resetRepTracking()
            return SquatFeedbackResult(repSummary = repSummary, bottomSummary = bottomSummary)
        }
        return SquatFeedbackResult(repSummary = repSummary, bottomSummary = bottomSummary)
    }

    private fun resetRepTracking() {
        repMinKneeAngle = null
        repMinTrunkToThighAngle = null
        repMaxTrunkTiltVerticalAngle = null
        repMaxHeelRise = null
        repMaxKneeForward = null
    }

    private fun accuracyFor(kneeAngle: Float): Float {
        val depthRange = SQUAT_STANDING_KNEE_ANGLE_THRESHOLD - SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
        val normalized = ((kneeAngle - SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD) / depthRange)
            .coerceIn(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_ONE)
        return (SQUAT_FLOAT_ONE - normalized).coerceIn(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_ONE)
    }

    private fun suppressStandingFeedback(
        counterResult: SquatRepCounterResult,
        feedbackType: PostureFeedbackType
    ): PostureFeedbackType =
        if (counterResult.phase == SquatPhase.UP &&
            counterResult.kneeAngleEma > repCounter.upThreshold() &&
            isFormCorrection(feedbackType)
        ) {
            PostureFeedbackType.GOOD_FORM
        } else {
            feedbackType
        }

    private fun isFormCorrection(type: PostureFeedbackType): Boolean =
        type == PostureFeedbackType.EXCESS_FORWARD_LEAN ||
                type == PostureFeedbackType.HEEL_RISE ||
                type == PostureFeedbackType.KNEE_FORWARD ||
                type == PostureFeedbackType.TOO_SHALLOW ||
                type == PostureFeedbackType.STAND_TALL

    private fun buildRepMetrics(counterResult: SquatRepCounterResult): SquatRepMetrics =
        SquatRepMetrics(
            minKneeAngle = repMinKneeAngle ?: counterResult.kneeAngleEma,
            minTrunkToThighAngle = repMinTrunkToThighAngle ?: counterResult.trunkToThighEma,
            maxTrunkTiltVerticalAngle =
                repMaxTrunkTiltVerticalAngle ?: counterResult.trunkTiltVerticalEma,
            maxHeelRiseRatio = repMaxHeelRise,
            maxKneeForwardRatio = repMaxKneeForward
        )

    private fun warningEventFor(
        feedbackType: PostureFeedbackType,
        summary: SquatRepSummary,
        phase: SquatPhase,
        timestampMs: Long
    ): PostureWarningEvent? = when (feedbackType) {
        PostureFeedbackType.EXCESS_FORWARD_LEAN ->
            PostureWarningEvent(
                feedbackType = feedbackType,
                metric = SquatWarningMetric.TRUNK_TO_THIGH,
                value = summary.minTrunkToThighAngle,
                threshold = SQUAT_TRUNK_TO_THIGH_ANGLE_HARD_THRESHOLD,
                operator = ComparisonOperator.LESS_THAN,
                phase = phase,
                timestampMs = timestampMs
            )

        PostureFeedbackType.TOO_SHALLOW ->
            PostureWarningEvent(
                feedbackType = feedbackType,
                metric = SquatWarningMetric.KNEE_ANGLE,
                value = summary.minKneeAngle,
                threshold = depthThreshold(summary),
                operator = ComparisonOperator.GREATER_THAN,
                phase = phase,
                timestampMs = timestampMs
            )

        PostureFeedbackType.HEEL_RISE -> summary.maxHeelRiseRatio?.let { value ->
            PostureWarningEvent(
                feedbackType = feedbackType,
                metric = SquatWarningMetric.HEEL_RISE_RATIO,
                value = value,
                threshold = SQUAT_HEEL_RISE_RATIO_THRESHOLD,
                operator = ComparisonOperator.GREATER_THAN,
                phase = phase,
                timestampMs = timestampMs
            )
        }

        PostureFeedbackType.KNEE_FORWARD -> summary.maxKneeForwardRatio?.let { value ->
            PostureWarningEvent(
                feedbackType = feedbackType,
                metric = SquatWarningMetric.KNEE_FORWARD_RATIO,
                value = value,
                threshold = SQUAT_KNEE_FORWARD_RATIO_THRESHOLD,
                operator = ComparisonOperator.GREATER_THAN,
                phase = phase,
                timestampMs = timestampMs
            )
        }

        else -> null
    }

    private fun depthThreshold(summary: SquatRepSummary): Float =
        if (summary.minKneeAngle > SQUAT_SHALLOW_DEPTH_ANGLE_THRESHOLD) {
            SQUAT_SHALLOW_DEPTH_ANGLE_THRESHOLD
        } else {
            SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
        }
}

private data class SquatFeedbackResult(
    val repSummary: SquatRepSummary?,
    val bottomSummary: SquatRepSummary?
)
