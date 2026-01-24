package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_BOTTOM_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_BOTTOM_FRAMES_REQUIRED
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_DEEP_FRONT
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_SHALLOW_BACK
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_SHALLOW_FRONT
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DESCENDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FULL_BODY_INVISIBLE_DURATION_MS
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_COLLAPSE_INWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_TOO_FORWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_MIN_LANDMARK_CONFIDENCE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REP_COMPLETE_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REP_COMPLETE_MARGIN
import com.jeong.runninggoaltracker.domain.contract.LUNGE_SCORE_PERFECT_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STANDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STABILITY_MIN_SAMPLES
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FRONT_KNEE_TARGET_MIN_ANGLE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_TOP_FRAMES_REQUIRED
import com.jeong.runninggoaltracker.domain.contract.LUNGE_TORSO_TOO_LEAN_FORWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_UNSTABLE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_ASCENDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.model.LungeDebugInfo
import com.jeong.runninggoaltracker.domain.model.LungeRepSummary
import com.jeong.runninggoaltracker.domain.model.PoseAnalysisResult
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
import com.jeong.runninggoaltracker.domain.model.PoseSide
import com.jeong.runninggoaltracker.domain.model.PostureFeedback
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.RepCount
import com.jeong.runninggoaltracker.domain.model.SquatFrameMetrics
import com.jeong.runninggoaltracker.domain.model.SquatPhase
import com.jeong.runninggoaltracker.domain.model.SquatState
import com.jeong.runninggoaltracker.domain.usecase.ExerciseAnalyzer
import kotlin.math.max
import kotlin.math.min

class LungeAnalyzer(
    private val debugLogger: (Any) -> Unit = {},
    private val metricsCalculator: LungeMetricsCalculator = LungeMetricsCalculator(
        debugLogger = debugLogger
    ),
    private val repCounter: LungeRepCounter = LungeRepCounter(
        debugLogger = debugLogger
    ),
    private val scorer: LungeFormScorer = LungeFormScorer(),
    private val leadLegSelector: LungeLeadLegSelector = LungeLeadLegSelector()
) : ExerciseAnalyzer {
    private var repMinFrontKneeAngle: Float? = null
    private var repMinBackKneeAngle: Float? = null
    private var repMaxTorsoLeanAngle: Float? = null
    private var repMaxKneeForwardRatio: Float? = null
    private var repMaxKneeCollapseRatio: Float? = null
    private val hipStats = RunningStats()
    private val shoulderStats = RunningStats()
    private var hipCenterMin: Float? = null
    private var hipCenterMax: Float? = null
    private var shoulderCenterMin: Float? = null
    private var shoulderCenterMax: Float? = null
    private var currentHipCenterX: Float? = null
    private var currentShoulderCenterX: Float? = null
    private var hasBottomFeedback: Boolean = false
    private var previousPhase: SquatPhase = SquatPhase.UP
    private var fullBodyInvisibleStartMs: Long? = null
    private var notInFrameFeedbackEmitted: Boolean = false
    private val kneeSanitizer = LungeKneeAngleSanitizer()
    private var lastLeftMetricValid: Float? = null
    private var lastRightMetricValid: Float? = null
    private var metricsTotalFrames: Int = LUNGE_INT_ZERO
    private var metricsNullFrames: Int = LUNGE_INT_ZERO
    private var metricsNullStreak: Int = LUNGE_INT_ZERO

    override fun analyze(frame: PoseFrame): PoseAnalysisResult {
        val metrics = metricsCalculator.calculate(frame)
        metricsTotalFrames += 1
        if (metrics == null) {
            metricsNullFrames += 1
            metricsNullStreak += 1
        } else {
            metricsNullStreak = LUNGE_INT_ZERO
        }
        val metricsNullRate = metricsNullFrames.toFloat() / metricsTotalFrames.toFloat()
        val fullBodyState = updateFullBodyVisibility(frame)
        val leftKneeAngleRaw =
            metrics?.leftKneeAngle ?: metricsCalculator.kneeAngle(frame, PoseSide.LEFT)
        val rightKneeAngleRaw =
            metrics?.rightKneeAngle ?: metricsCalculator.kneeAngle(frame, PoseSide.RIGHT)
        val leftSanitizeResult =
            kneeSanitizer.sanitize(leftKneeAngleRaw, lastLeftMetricValid, PoseSide.LEFT)
        val rightSanitizeResult =
            kneeSanitizer.sanitize(rightKneeAngleRaw, lastRightMetricValid, PoseSide.RIGHT)
        val leftKneeAngle = leftSanitizeResult.angle
        val rightKneeAngle = rightSanitizeResult.angle
        if (leftKneeAngle != null) {
            lastLeftMetricValid = leftKneeAngle
        }
        if (rightKneeAngle != null) {
            lastRightMetricValid = rightKneeAngle
        }
        val leadLegSelection = leadLegSelector.update(metrics, leftKneeAngle, rightKneeAngle)
            ?: leadLegFromAngles(leftKneeAngle, rightKneeAngle)
        val counterResult = repCounter.update(
            timestampMs = frame.timestampMs,
            metrics = metrics,
            leftKneeAngle = leftKneeAngleRaw,
            rightKneeAngle = rightKneeAngleRaw,
            leadLeg = leadLegSelection
        )
        val skippedLowConfidence = metrics == null
        if (counterResult == null) {
            val feedbackType = if (fullBodyState.isVisible) {
                PostureFeedbackType.UNKNOWN
            } else {
                PostureFeedbackType.NOT_IN_FRAME
            }
            return PoseAnalysisResult(
                repCount = RepCount(LUNGE_INT_ZERO, isIncremented = false),
                feedback = PostureFeedback(
                    type = feedbackType,
                    isValid = false,
                    accuracy = LUNGE_FLOAT_ZERO,
                    isPerfectForm = false
                ),
                feedbackEvent = fullBodyState.feedbackEvent,
                feedbackEventKey = null,
                frameMetrics = null,
                repSummary = null,
                lungeRepSummary = null,
                warningEvent = null,
                feedbackKeys = emptyList(),
                skippedLowConfidence = skippedLowConfidence
            )
        }
        val activeSide = counterResult.countingSide ?: leadLegSelection ?: PoseSide.LEFT
        val frontKneeAngle = if (activeSide == PoseSide.LEFT) leftKneeAngle else rightKneeAngle
        val backKneeAngle = if (activeSide == PoseSide.LEFT) rightKneeAngle else leftKneeAngle
        val repTrackingResult = updateRepTracking(
            counterResult,
            metrics,
            frontKneeAngle,
            backKneeAngle,
            activeSide
        )
        val feedbackResult =
            handleFeedback(counterResult, metrics, frontKneeAngle, backKneeAngle, activeSide, frame)
        val feedbackSummary = feedbackResult.repSummary ?: feedbackResult.bottomSummary
        val feedbackEventKey = when {
            feedbackResult.repSummary != null -> feedbackResult.repSummary.feedbackKeys.firstOrNull()
            feedbackResult.bottomSummary != null -> feedbackResult.bottomSummary.feedbackKeys.firstOrNull()
            else -> null
        }
        val feedbackKeys = if (!counterResult.isReliable || !fullBodyState.isVisible) {
            emptyList()
        } else {
            listOfNotNull(feedbackEventKey)
        }
        val feedbackType = when {
            !counterResult.isReliable || !fullBodyState.isVisible -> PostureFeedbackType.NOT_IN_FRAME
            feedbackSummary == null -> PostureFeedbackType.GOOD_FORM
            feedbackKeys.isNotEmpty() -> feedbackTypeForKey(feedbackKeys.first())
            else -> PostureFeedbackType.GOOD_FORM
        }
        val accuracy = accuracyFor(counterResult.kneeAngleEma)
        val isPerfectForm = feedbackSummary?.overallScore?.toFloat()?.let {
            it >= LUNGE_SCORE_PERFECT_THRESHOLD
        } ?: false
        val kneeAngleRaw = counterResult.kneeAngleRaw
        val trunkToThigh = metrics?.let { frontTrunkToThigh(it, activeSide) } ?: LUNGE_FLOAT_ZERO
        val stabilityInfo = stabilityStdDev()
        val unstableEmitted = feedbackSummary?.feedbackKeys?.contains(LUNGE_UNSTABLE) == true
        val normalizedRangeOk = isNormalizedRange(currentHipCenterX) &&
                isNormalizedRange(currentShoulderCenterX)
        val lungeDebugInfo = LungeDebugInfo(
            activeSide = activeSide,
            countingSide = counterResult.countingSide,
            leftKneeAngleRaw = leftKneeAngleRaw,
            rightKneeAngleRaw = rightKneeAngleRaw,
            leftKneeAngleSanitized = leftKneeAngle,
            rightKneeAngleSanitized = rightKneeAngle,
            lastLeftKneeAngle = lastLeftMetricValid,
            lastRightKneeAngle = lastRightMetricValid,
            leftOutlierReason = leftSanitizeResult.outlier?.reason,
            rightOutlierReason = rightSanitizeResult.outlier?.reason,
            repMinUpdated = repTrackingResult.repMinUpdated,
            hipSampleCount = hipStats.sampleCount(),
            shoulderSampleCount = shoulderStats.sampleCount(),
            hipCenterX = currentHipCenterX,
            shoulderCenterX = currentShoulderCenterX,
            hipCenterMin = hipCenterMin,
            hipCenterMax = hipCenterMax,
            shoulderCenterMin = shoulderCenterMin,
            shoulderCenterMax = shoulderCenterMax,
            stabilityStdDev = stabilityInfo.value,
            stabilityEligible = stabilityInfo.isEligible,
            stabilityNormalized = normalizedRangeOk,
            feedbackEventKey = feedbackEventKey,
            state = counterResult.state,
            phase = counterResult.phase,
            isReliable = counterResult.isReliable,
            standingThreshold = LUNGE_STANDING_KNEE_ANGLE_THRESHOLD,
            descendingThreshold = LUNGE_DESCENDING_KNEE_ANGLE_THRESHOLD,
            bottomThreshold = LUNGE_BOTTOM_KNEE_ANGLE_THRESHOLD,
            ascendingThreshold = LUNGE_ASCENDING_KNEE_ANGLE_THRESHOLD,
            repCompleteThreshold = LUNGE_REP_COMPLETE_KNEE_ANGLE_THRESHOLD - LUNGE_REP_COMPLETE_MARGIN,
            hysteresisFrames = counterResult.hysteresisFrames,
            standingToDescendingCount = counterResult.standingToDescendingCount,
            descendingToBottomCount = counterResult.descendingToBottomCount,
            descendingToStandingCount = counterResult.descendingToStandingCount,
            bottomToAscendingCount = counterResult.bottomToAscendingCount,
            ascendingToCompleteCount = counterResult.ascendingToCompleteCount,
            repCompleteToStandingCount = counterResult.repCompleteToStandingCount,
            repCompleteToDescendingCount = counterResult.repCompleteToDescendingCount,
            metricsNullRate = metricsNullRate,
            metricsNullStreak = metricsNullStreak
        )
        debugLogger(
            LungeAnalyzerDebug(
                metricsAvailable = metrics != null,
                leftKneeAngle = leftKneeAngle,
                rightKneeAngle = rightKneeAngle,
                frontKneeAngle = frontKneeAngle,
                leadLegSelection = leadLegSelection,
                leadLeg = activeSide,
                countingKneeAngle = counterResult.countingKneeAngle,
                countingSide = counterResult.countingSide,
                activeSide = activeSide,
                leftKneeAngleRaw = leftKneeAngleRaw,
                rightKneeAngleRaw = rightKneeAngleRaw,
                leftKneeAngleSanitized = leftKneeAngle,
                rightKneeAngleSanitized = rightKneeAngle,
                leftOutlier = leftSanitizeResult.outlier,
                rightOutlier = rightSanitizeResult.outlier,
                repMinUpdated = repTrackingResult.repMinUpdated,
                isFrontCamera = frame.isFrontCamera,
                isMirrored = frame.isMirrored,
                counterState = counterResult.state,
                counterPhase = counterResult.phase,
                counterReliable = counterResult.isReliable,
                repCompleted = counterResult.repCompleted,
                repCount = counterResult.repCount,
                hipSampleCount = hipStats.sampleCount(),
                shoulderSampleCount = shoulderStats.sampleCount(),
                stabilityStdDev = stabilityInfo.value,
                stabilityEligible = stabilityInfo.isEligible,
                stabilityNormalized = normalizedRangeOk,
                feedbackSummaryAvailable = feedbackSummary != null,
                unstableEmitted = unstableEmitted,
                feedbackEventKey = feedbackEventKey
            )
        )
        val frameMetrics = SquatFrameMetrics(
            kneeAngleRaw = kneeAngleRaw,
            kneeAngleEma = counterResult.kneeAngleEma,
            trunkTiltVerticalAngleRaw = counterResult.trunkTiltVerticalRaw,
            trunkTiltVerticalAngleEma = counterResult.trunkTiltVerticalEma,
            trunkToThighAngleRaw = trunkToThigh,
            trunkToThighAngleEma = trunkToThigh,
            heelRiseRatio = null,
            kneeForwardRatio = frontKneeForwardRatio(metrics, activeSide),
            phase = counterResult.phase,
            side = activeSide,
            isSideLocked = true,
            upThreshold = LUNGE_STANDING_KNEE_ANGLE_THRESHOLD,
            downThreshold = LUNGE_BOTTOM_KNEE_ANGLE_THRESHOLD,
            upFramesRequired = LUNGE_TOP_FRAMES_REQUIRED,
            downFramesRequired = LUNGE_BOTTOM_FRAMES_REQUIRED,
            upCandidateFrames = counterResult.upCandidateFrames,
            downCandidateFrames = counterResult.downCandidateFrames,
            repMinKneeAngle = repMinFrontKneeAngle ?: counterResult.kneeAngleEma,
            repMinTrunkToThighAngle = trunkToThigh,
            repMaxTrunkTiltVerticalAngle = repMaxTorsoLeanAngle
                ?: counterResult.trunkTiltVerticalEma,
            attemptActive = false,
            depthReached = false,
            attemptMinKneeAngle = repMinFrontKneeAngle ?: counterResult.kneeAngleEma,
            fullBodyVisible = fullBodyState.isVisible,
            fullBodyInvisibleDurationMs = fullBodyState.invisibleDurationMs,
            leftConfidenceSum = LUNGE_FLOAT_ZERO,
            rightConfidenceSum = LUNGE_FLOAT_ZERO,
            rotationDegrees = frame.rotationDegrees,
            isFrontCamera = frame.isFrontCamera,
            isMirroringApplied = frame.isMirrored,
            isCameraTiltSuspected = false,
            transition = counterResult.transition,
            isLandmarkReliable = counterResult.isReliable,
            isCalibrated = true
        )
        val feedbackEvent = when {
            fullBodyState.feedbackEvent != null -> fullBodyState.feedbackEvent
            counterResult.repCompleted && fullBodyState.isVisible -> feedbackType
            else -> null
        }
        previousPhase = counterResult.phase
        if (counterResult.repCompleted) {
            leadLegSelector.reset()
            lastLeftMetricValid = null
            lastRightMetricValid = null
        }
        return PoseAnalysisResult(
            repCount = RepCount(counterResult.repCount, isIncremented = counterResult.repCompleted),
            feedback = PostureFeedback(
                type = feedbackType,
                isValid = feedbackSummary != null,
                accuracy = accuracy,
                isPerfectForm = isPerfectForm
            ),
            feedbackEvent = feedbackEvent,
            feedbackEventKey = feedbackEventKey,
            frameMetrics = frameMetrics,
            repSummary = null,
            lungeRepSummary = feedbackSummary,
            warningEvent = null,
            feedbackKeys = feedbackKeys,
            skippedLowConfidence = skippedLowConfidence,
            lungeDebugInfo = lungeDebugInfo
        )
    }

    private fun updateRepTracking(
        counterResult: LungeRepCounterResult,
        metrics: LungeRawMetrics?,
        frontKneeAngle: Float?,
        backKneeAngle: Float?,
        activeSide: PoseSide
    ): RepTrackingUpdate {
        if (counterResult.phase == SquatPhase.DOWN && previousPhase == SquatPhase.UP) {
            resetRepTracking()
            hasBottomFeedback = false
        }
        if (metrics == null || frontKneeAngle == null || backKneeAngle == null) {
            return RepTrackingUpdate(repMinUpdated = false)
        }
        if (counterResult.phase == SquatPhase.DOWN &&
            (counterResult.state == SquatState.DESCENDING || counterResult.state == SquatState.BOTTOM)
        ) {
            repMinFrontKneeAngle = repMinFrontKneeAngle?.let { min(it, frontKneeAngle) }
                ?: frontKneeAngle
            repMinBackKneeAngle =
                repMinBackKneeAngle?.let { min(it, backKneeAngle) } ?: backKneeAngle
            repMaxTorsoLeanAngle = repMaxTorsoLeanAngle?.let {
                max(it, counterResult.trunkTiltVerticalEma)
            } ?: counterResult.trunkTiltVerticalEma
            repMaxKneeForwardRatio =
                updateMaxRatio(repMaxKneeForwardRatio, frontKneeForwardRatio(metrics, activeSide))
            repMaxKneeCollapseRatio =
                updateMaxRatio(repMaxKneeCollapseRatio, frontKneeCollapseRatio(metrics, activeSide))
            updateStability(metrics)
            return RepTrackingUpdate(repMinUpdated = true)
        }
        return RepTrackingUpdate(repMinUpdated = false)
    }

    private fun handleFeedback(
        counterResult: LungeRepCounterResult,
        metrics: LungeRawMetrics?,
        frontKneeAngle: Float?,
        backKneeAngle: Float?,
        leadLeg: PoseSide,
        frame: PoseFrame
    ): LungeFeedbackResult {
        var bottomSummary: LungeRepSummary? = null
        var repSummary: LungeRepSummary? = null
        if (counterResult.phase == SquatPhase.DOWN && metrics != null && frontKneeAngle != null && backKneeAngle != null) {
            if (!hasBottomFeedback && counterResult.state == SquatState.BOTTOM) {
                bottomSummary = scorer.score(
                    buildRepMetrics(
                        metrics,
                        frontKneeAngle,
                        backKneeAngle,
                        leadLeg,
                        frame
                    )
                )
                hasBottomFeedback = true
            }
        }
        if (counterResult.repCompleted && metrics != null && frontKneeAngle != null && backKneeAngle != null) {
            repSummary = scorer.score(
                buildRepMetrics(
                    metrics,
                    frontKneeAngle,
                    backKneeAngle,
                    leadLeg,
                    frame
                )
            )
            resetRepTracking()
            hasBottomFeedback = false
        }
        return LungeFeedbackResult(repSummary = repSummary, bottomSummary = bottomSummary)
    }

    private fun buildRepMetrics(
        metrics: LungeRawMetrics,
        frontKneeAngle: Float,
        backKneeAngle: Float,
        leadLeg: PoseSide,
        frame: PoseFrame
    ): LungeRepMetrics {
        val stabilityStdDev = stabilityStdDev().value
        return LungeRepMetrics(
            frontKneeMinAngle = repMinFrontKneeAngle ?: frontKneeAngle,
            backKneeMinAngle = repMinBackKneeAngle ?: backKneeAngle,
            maxKneeForwardRatio = repMaxKneeForwardRatio,
            maxTorsoLeanAngle = repMaxTorsoLeanAngle ?: metrics.trunkTiltVerticalAngle,
            maxKneeCollapseRatio = repMaxKneeCollapseRatio,
            stabilityStdDev = stabilityStdDev,
            isFrontCamera = frame.isFrontCamera,
            frontLeg = leadLeg
        )
    }

    private fun updateStability(metrics: LungeRawMetrics) {
        val width = LUNGE_FLOAT_ONE
        val hipCenter = metrics.hipCenterX / width
        val shoulderCenter = metrics.shoulderCenterX / width
        currentHipCenterX = hipCenter
        currentShoulderCenterX = shoulderCenter
        hipStats.update(hipCenter)
        shoulderStats.update(shoulderCenter)
        hipCenterMin = hipCenterMin?.let { min(it, hipCenter) } ?: hipCenter
        hipCenterMax = hipCenterMax?.let { max(it, hipCenter) } ?: hipCenter
        shoulderCenterMin = shoulderCenterMin?.let { min(it, shoulderCenter) } ?: shoulderCenter
        shoulderCenterMax = shoulderCenterMax?.let { max(it, shoulderCenter) } ?: shoulderCenter
    }

    private fun stabilityStdDev(): StabilityStdDev {
        val hipCount = hipStats.sampleCount()
        val shoulderCount = shoulderStats.sampleCount()
        val isEligible = hipCount >= LUNGE_STABILITY_MIN_SAMPLES &&
                shoulderCount >= LUNGE_STABILITY_MIN_SAMPLES
        if (!isEligible) {
            return StabilityStdDev(value = LUNGE_FLOAT_ZERO, isEligible = false)
        }
        val hipStdDev = hipStats.standardDeviation()
        val shoulderStdDev = shoulderStats.standardDeviation()
        val maxStdDev = max(hipStdDev, shoulderStdDev)
        val safeStdDev = if (maxStdDev.isNaN() || maxStdDev.isInfinite()) {
            LUNGE_FLOAT_ZERO
        } else {
            maxStdDev
        }
        return StabilityStdDev(value = safeStdDev, isEligible = true)
    }

    private fun updateMaxRatio(current: Float?, candidate: Float?): Float? =
        if (candidate == null) {
            current
        } else {
            current?.let { max(it, candidate) } ?: candidate
        }

    private fun resetRepTracking() {
        repMinFrontKneeAngle = null
        repMinBackKneeAngle = null
        repMaxTorsoLeanAngle = null
        repMaxKneeForwardRatio = null
        repMaxKneeCollapseRatio = null
        hipStats.reset()
        shoulderStats.reset()
        hipCenterMin = null
        hipCenterMax = null
        shoulderCenterMin = null
        shoulderCenterMax = null
        currentHipCenterX = null
        currentShoulderCenterX = null
    }

    private fun isNormalizedRange(value: Float?): Boolean =
        value != null && value >= LUNGE_FLOAT_ZERO && value <= LUNGE_FLOAT_ONE

    private fun frontTrunkToThigh(metrics: LungeRawMetrics, leadLeg: PoseSide): Float =
        if (leadLeg == PoseSide.LEFT) {
            metrics.leftTrunkToThighAngle
        } else {
            metrics.rightTrunkToThighAngle
        }

    private fun frontKneeForwardRatio(metrics: LungeRawMetrics?, leadLeg: PoseSide): Float? =
        metrics?.let {
            if (leadLeg == PoseSide.LEFT) it.leftKneeForwardRatio else it.rightKneeForwardRatio
        }

    private fun frontKneeCollapseRatio(metrics: LungeRawMetrics?, leadLeg: PoseSide): Float? =
        metrics?.let {
            if (leadLeg == PoseSide.LEFT) it.leftKneeCollapseRatio else it.rightKneeCollapseRatio
        }

    private fun leadLegFromAngles(leftKneeAngle: Float?, rightKneeAngle: Float?): PoseSide? = when {
        leftKneeAngle == null && rightKneeAngle == null -> null
        rightKneeAngle == null -> PoseSide.LEFT
        leftKneeAngle == null -> PoseSide.RIGHT
        leftKneeAngle < rightKneeAngle -> PoseSide.LEFT
        rightKneeAngle < leftKneeAngle -> PoseSide.RIGHT
        else -> null
    }

    private fun feedbackTypeForKey(key: String): PostureFeedbackType = when (key) {
        LUNGE_DEPTH_TOO_SHALLOW_FRONT,
        LUNGE_DEPTH_TOO_SHALLOW_BACK,
        LUNGE_DEPTH_TOO_DEEP_FRONT ->
            PostureFeedbackType.TOO_SHALLOW

        LUNGE_KNEE_TOO_FORWARD,
        LUNGE_KNEE_COLLAPSE_INWARD ->
            PostureFeedbackType.KNEE_FORWARD

        LUNGE_TORSO_TOO_LEAN_FORWARD ->
            PostureFeedbackType.EXCESS_FORWARD_LEAN

        LUNGE_UNSTABLE -> PostureFeedbackType.STAND_TALL
        else -> PostureFeedbackType.GOOD_FORM
    }

    private fun accuracyFor(kneeAngle: Float): Float {
        val depthRange = LUNGE_STANDING_KNEE_ANGLE_THRESHOLD - LUNGE_FRONT_KNEE_TARGET_MIN_ANGLE
        val normalized = ((kneeAngle - LUNGE_FRONT_KNEE_TARGET_MIN_ANGLE) / depthRange)
            .coerceIn(LUNGE_FLOAT_ZERO, LUNGE_FLOAT_ONE)
        return (LUNGE_FLOAT_ONE - normalized).coerceIn(LUNGE_FLOAT_ZERO, LUNGE_FLOAT_ONE)
    }

    private fun updateFullBodyVisibility(frame: PoseFrame): FullBodyVisibilityState {
        val isVisible = isFullBodyVisible(frame)
        val startMs = fullBodyInvisibleStartMs
        return if (isVisible) {
            fullBodyInvisibleStartMs = null
            notInFrameFeedbackEmitted = false
            FullBodyVisibilityState(
                isVisible = true,
                invisibleDurationMs = LUNGE_INT_ZERO.toLong(),
                feedbackEvent = null
            )
        } else {
            val startTimestamp = startMs ?: frame.timestampMs
            fullBodyInvisibleStartMs = startTimestamp
            val durationMs = frame.timestampMs - startTimestamp
            val shouldEmit = durationMs >= LUNGE_FULL_BODY_INVISIBLE_DURATION_MS &&
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
                nose.confidence >= LUNGE_MIN_LANDMARK_CONFIDENCE &&
                leftShoulder.confidence >= LUNGE_MIN_LANDMARK_CONFIDENCE &&
                rightShoulder.confidence >= LUNGE_MIN_LANDMARK_CONFIDENCE &&
                leftAnkle.confidence >= LUNGE_MIN_LANDMARK_CONFIDENCE &&
                rightAnkle.confidence >= LUNGE_MIN_LANDMARK_CONFIDENCE
    }
}

private data class LungeAnalyzerDebug(
    val metricsAvailable: Boolean,
    val leftKneeAngle: Float?,
    val rightKneeAngle: Float?,
    val frontKneeAngle: Float?,
    val leadLegSelection: PoseSide?,
    val leadLeg: PoseSide,
    val countingKneeAngle: Float?,
    val countingSide: PoseSide?,
    val activeSide: PoseSide,
    val leftKneeAngleRaw: Float?,
    val rightKneeAngleRaw: Float?,
    val leftKneeAngleSanitized: Float?,
    val rightKneeAngleSanitized: Float?,
    val leftOutlier: LungeKneeAngleOutlier?,
    val rightOutlier: LungeKneeAngleOutlier?,
    val repMinUpdated: Boolean,
    val isFrontCamera: Boolean,
    val isMirrored: Boolean,
    val counterState: SquatState?,
    val counterPhase: SquatPhase?,
    val counterReliable: Boolean?,
    val repCompleted: Boolean?,
    val repCount: Int?,
    val hipSampleCount: Int,
    val shoulderSampleCount: Int,
    val stabilityStdDev: Float,
    val stabilityEligible: Boolean,
    val stabilityNormalized: Boolean,
    val feedbackSummaryAvailable: Boolean,
    val unstableEmitted: Boolean,
    val feedbackEventKey: String?
)

private data class StabilityStdDev(
    val value: Float,
    val isEligible: Boolean
)

private data class LungeFeedbackResult(
    val repSummary: LungeRepSummary?,
    val bottomSummary: LungeRepSummary?
)

private data class RepTrackingUpdate(
    val repMinUpdated: Boolean
)

private data class FullBodyVisibilityState(
    val isVisible: Boolean,
    val invisibleDurationMs: Long,
    val feedbackEvent: PostureFeedbackType?
)
