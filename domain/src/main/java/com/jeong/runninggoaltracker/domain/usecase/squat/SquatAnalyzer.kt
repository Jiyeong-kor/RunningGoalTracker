package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_CALIBRATION_REQUIRED_FRAMES
import com.jeong.runninggoaltracker.domain.contract.SQUAT_DESCENDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_HEEL_RISE_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FULL_BODY_INVISIBLE_DURATION_MS
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_KNEE_FORWARD_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_MIN_LANDMARK_CONFIDENCE
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
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
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
    private var attemptActive: Boolean = false
    private var attemptDepthReached: Boolean = false
    private var attemptMinKneeAngle: Float? = null
    private var attemptDepthFrames: Int = SQUAT_INT_ZERO
    private var fullBodyInvisibleStartMs: Long? = null
    private var notInFrameFeedbackEmitted: Boolean = false
    private var wasStableUp: Boolean = true

    override fun analyze(frame: PoseFrame): PoseAnalysisResult {
        val sideSelection = sideSelector.update(frame, previousPhase)
        val metrics =
            sideSelection.selectedSide?.let { metricsCalculator.calculate(frame, calibration, it) }
        val fullBodyState = updateFullBodyVisibility(frame)
        val fullBodyVisible = fullBodyState.isVisible
        val counterResult = repCounter.update(frame.timestampMs, metrics)
        val skippedLowConfidence = metrics == null
        if (counterResult == null) {
            val feedbackType = if (fullBodyVisible) {
                PostureFeedbackType.UNKNOWN
            } else {
                PostureFeedbackType.NOT_IN_FRAME
            }
            return PoseAnalysisResult(
                repCount = RepCount(SQUAT_INT_ZERO, isIncremented = false),
                feedback = PostureFeedback(
                    type = feedbackType,
                    isValid = false,
                    accuracy = SQUAT_FLOAT_ZERO,
                    isPerfectForm = false
                ),
                feedbackEvent = fullBodyState.feedbackEvent,
                frameMetrics = null,
                repSummary = null,
                lungeRepSummary = null,
                warningEvent = null,
                feedbackKeys = emptyList(),
                skippedLowConfidence = skippedLowConfidence
            )
        }
        if (calibration == null && counterResult.isReliable && counterResult.phase == SquatPhase.UP) {
            metrics?.let { accumulateCalibration(it) }
        }
        val attemptResult = updateAttemptState(counterResult, fullBodyVisible)
        val feedbackResult = handleRepTracking(counterResult, metrics)
        val feedbackSummary = feedbackResult.repSummary ?: feedbackResult.bottomSummary
        val rawFeedbackType = when {
            !counterResult.isReliable -> PostureFeedbackType.NOT_IN_FRAME
            feedbackSummary != null -> qualityEvaluator.feedbackType(feedbackSummary)
                .also { lastFeedbackType = it }

            else -> lastFeedbackType
        }
        val fullBodyFeedbackType =
            if (fullBodyVisible) rawFeedbackType else PostureFeedbackType.NOT_IN_FRAME
        val feedbackType = suppressStandingFeedback(counterResult, fullBodyFeedbackType)
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
        val attemptMinKnee = attemptMinKneeAngle ?: counterResult.kneeAngleEma
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
            attemptActive = attemptActive,
            depthReached = attemptDepthReached,
            attemptMinKneeAngle = attemptMinKnee,
            fullBodyVisible = fullBodyVisible,
            fullBodyInvisibleDurationMs = fullBodyState.invisibleDurationMs,
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
            if (fullBodyVisible &&
                feedbackType == rawFeedbackType &&
                isFormCorrection(feedbackType)
            ) {
                warningEventFor(feedbackType, summary, counterResult.phase, frame.timestampMs)
            } else {
                null
            }
        }
        val stableUpEvent = shouldEmitStableUpEvent(counterResult, fullBodyVisible)
        val feedbackEvent = when {
            fullBodyState.feedbackEvent != null -> fullBodyState.feedbackEvent
            attemptResult.shallowFeedbackEvent -> PostureFeedbackType.TOO_SHALLOW
            counterResult.repCompleted && fullBodyVisible -> feedbackType
            stableUpEvent -> PostureFeedbackType.GOOD_FORM
            else -> null
        }
        if (feedbackEvent != null) {
            lastFeedbackType = feedbackEvent
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
            feedbackEvent = feedbackEvent,
            frameMetrics = frameMetrics,
            repSummary = feedbackResult.repSummary,
            lungeRepSummary = null,
            warningEvent = warningEvent,
            feedbackKeys = emptyList(),
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

    private fun updateAttemptState(
        counterResult: SquatRepCounterResult,
        fullBodyVisible: Boolean
    ): AttemptUpdateResult {
        val kneeAngle = counterResult.kneeAngleEma
        val isReliable = counterResult.isReliable
        var shallowFeedbackEvent = false
        if (!attemptActive &&
            fullBodyVisible &&
            isReliable &&
            kneeAngle <= SQUAT_DESCENDING_KNEE_ANGLE_THRESHOLD
        ) {
            attemptActive = true
            attemptMinKneeAngle = kneeAngle
            attemptDepthReached = false
            attemptDepthFrames = SQUAT_INT_ZERO
            wasStableUp = false
        }
        if (attemptActive) {
            if (isReliable) {
                attemptMinKneeAngle = attemptMinKneeAngle?.let { min(it, kneeAngle) } ?: kneeAngle
                if (kneeAngle <= repCounter.downThreshold()) {
                    attemptDepthFrames += SQUAT_INT_ONE
                    if (attemptDepthFrames >= repCounter.downFramesRequired()) {
                        attemptDepthReached = true
                    }
                } else {
                    attemptDepthFrames = SQUAT_INT_ZERO
                }
                if (kneeAngle >= repCounter.upThreshold()) {
                    if (!attemptDepthReached && fullBodyVisible) {
                        shallowFeedbackEvent = true
                    }
                    attemptDepthFrames = SQUAT_INT_ZERO
                    attemptActive = false
                }
            } else {
                attemptDepthFrames = SQUAT_INT_ZERO
            }
        }
        return AttemptUpdateResult(shallowFeedbackEvent = shallowFeedbackEvent)
    }

    private fun updateFullBodyVisibility(frame: PoseFrame): FullBodyVisibilityState {
        val isVisible = isFullBodyVisible(frame)
        val startMs = fullBodyInvisibleStartMs
        return if (isVisible) {
            fullBodyInvisibleStartMs = null
            notInFrameFeedbackEmitted = false
            FullBodyVisibilityState(
                isVisible = true,
                invisibleDurationMs = SQUAT_INT_ZERO.toLong(),
                feedbackEvent = null
            )
        } else {
            val startTimestamp = startMs ?: frame.timestampMs
            fullBodyInvisibleStartMs = startTimestamp
            val durationMs = frame.timestampMs - startTimestamp
            val shouldEmit = durationMs >= SQUAT_FULL_BODY_INVISIBLE_DURATION_MS &&
                    !notInFrameFeedbackEmitted
            if (shouldEmit) {
                notInFrameFeedbackEmitted = true
            }
            FullBodyVisibilityState(
                isVisible = false,
                invisibleDurationMs = durationMs,
                feedbackEvent = if (shouldEmit) PostureFeedbackType.NOT_IN_FRAME else null
            )
        }
    }

    private fun shouldEmitStableUpEvent(
        counterResult: SquatRepCounterResult,
        fullBodyVisible: Boolean
    ): Boolean {
        val stableUp = fullBodyVisible &&
                counterResult.phase == SquatPhase.UP &&
                counterResult.kneeAngleEma >= repCounter.upThreshold() &&
                !attemptActive
        val shouldEmit = stableUp && !wasStableUp
        wasStableUp = stableUp
        return shouldEmit
    }

    private fun isFullBodyVisible(frame: PoseFrame): Boolean {
        val nose = frame.landmark(PoseLandmarkType.NOSE)
        val leftShoulder = frame.landmark(PoseLandmarkType.LEFT_SHOULDER)
        val rightShoulder = frame.landmark(PoseLandmarkType.RIGHT_SHOULDER)
        val leftAnkle = frame.landmark(PoseLandmarkType.LEFT_ANKLE)
        val rightAnkle = frame.landmark(PoseLandmarkType.RIGHT_ANKLE)
        return nose != null &&
                leftShoulder != null &&
                rightShoulder != null &&
                leftAnkle != null &&
                rightAnkle != null &&
                nose.confidence >= SQUAT_MIN_LANDMARK_CONFIDENCE &&
                leftShoulder.confidence >= SQUAT_MIN_LANDMARK_CONFIDENCE &&
                rightShoulder.confidence >= SQUAT_MIN_LANDMARK_CONFIDENCE &&
                leftAnkle.confidence >= SQUAT_MIN_LANDMARK_CONFIDENCE &&
                rightAnkle.confidence >= SQUAT_MIN_LANDMARK_CONFIDENCE
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

private data class AttemptUpdateResult(
    val shallowFeedbackEvent: Boolean
)

private data class FullBodyVisibilityState(
    val isVisible: Boolean,
    val invisibleDurationMs: Long,
    val feedbackEvent: PostureFeedbackType?
)
