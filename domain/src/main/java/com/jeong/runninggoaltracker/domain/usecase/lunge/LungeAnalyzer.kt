package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_BOTTOM_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_BOTTOM_FRAMES_REQUIRED
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_DEEP_FRONT
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_SHALLOW_BACK
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_SHALLOW_FRONT
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FULL_BODY_INVISIBLE_DURATION_MS
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_COLLAPSE_INWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_TOO_FORWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_MIN_LANDMARK_CONFIDENCE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_SCORE_PERFECT_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STANDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FRONT_KNEE_TARGET_MIN_ANGLE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_TOP_FRAMES_REQUIRED
import com.jeong.runninggoaltracker.domain.contract.LUNGE_TORSO_TOO_LEAN_FORWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_UNSTABLE
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
    private val metricsCalculator: LungeMetricsCalculator = LungeMetricsCalculator(),
    private val repCounter: LungeRepCounter = LungeRepCounter(),
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
    private var hasBottomFeedback: Boolean = false
    private var lastFeedbackType: PostureFeedbackType = PostureFeedbackType.UNKNOWN
    private var lastFeedbackKeys: List<String> = emptyList()
    private var previousPhase: SquatPhase = SquatPhase.UP
    private var fullBodyInvisibleStartMs: Long? = null
    private var notInFrameFeedbackEmitted: Boolean = false

    override fun analyze(frame: PoseFrame): PoseAnalysisResult {
        val metrics = metricsCalculator.calculate(frame)
        val fullBodyState = updateFullBodyVisibility(frame)
        val leadLeg = leadLegSelector.update(metrics) ?: PoseSide.LEFT
        val frontKneeAngle = metrics?.let { frontKneeAngle(it, leadLeg) }
        val backKneeAngle = metrics?.let { backKneeAngle(it, leadLeg) }
        val counterResult = repCounter.update(frame.timestampMs, metrics, frontKneeAngle)
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
                frameMetrics = null,
                repSummary = null,
                lungeRepSummary = null,
                warningEvent = null,
                feedbackKeys = emptyList(),
                skippedLowConfidence = skippedLowConfidence
            )
        }
        updateRepTracking(counterResult, metrics, frontKneeAngle, backKneeAngle, leadLeg, frame)
        val feedbackResult =
            handleFeedback(counterResult, metrics, frontKneeAngle, backKneeAngle, leadLeg, frame)
        val feedbackSummary = feedbackResult.repSummary ?: feedbackResult.bottomSummary
        val feedbackKeys = if (!counterResult.isReliable || !fullBodyState.isVisible) {
            emptyList()
        } else {
            feedbackSummary?.feedbackKeys ?: lastFeedbackKeys
        }
        val feedbackType = when {
            !counterResult.isReliable || !fullBodyState.isVisible -> PostureFeedbackType.NOT_IN_FRAME
            feedbackKeys.isNotEmpty() -> feedbackTypeForKey(feedbackKeys.first())
            else -> lastFeedbackType
        }
        lastFeedbackType = feedbackType
        lastFeedbackKeys = feedbackKeys
        val accuracy = accuracyFor(counterResult.kneeAngleEma)
        val isPerfectForm = feedbackSummary?.overallScore?.toFloat()?.let {
            it >= LUNGE_SCORE_PERFECT_THRESHOLD
        } ?: false
        val kneeAngleRaw = counterResult.kneeAngleRaw
        val trunkToThigh = metrics?.let { frontTrunkToThigh(it, leadLeg) } ?: LUNGE_FLOAT_ZERO
        val frameMetrics = SquatFrameMetrics(
            kneeAngleRaw = kneeAngleRaw,
            kneeAngleEma = counterResult.kneeAngleEma,
            trunkTiltVerticalAngleRaw = counterResult.trunkTiltVerticalRaw,
            trunkTiltVerticalAngleEma = counterResult.trunkTiltVerticalEma,
            trunkToThighAngleRaw = trunkToThigh,
            trunkToThighAngleEma = trunkToThigh,
            heelRiseRatio = null,
            kneeForwardRatio = frontKneeForwardRatio(metrics, leadLeg),
            phase = counterResult.phase,
            side = leadLeg,
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
            repSummary = null,
            lungeRepSummary = feedbackSummary,
            warningEvent = null,
            feedbackKeys = feedbackKeys,
            skippedLowConfidence = skippedLowConfidence
        )
    }

    private fun updateRepTracking(
        counterResult: LungeRepCounterResult,
        metrics: LungeRawMetrics?,
        frontKneeAngle: Float?,
        backKneeAngle: Float?,
        leadLeg: PoseSide,
        frame: PoseFrame
    ) {
        if (counterResult.phase == SquatPhase.DOWN && previousPhase == SquatPhase.UP) {
            resetRepTracking()
            hasBottomFeedback = false
        }
        if (metrics == null || frontKneeAngle == null || backKneeAngle == null) return
        if (counterResult.phase == SquatPhase.DOWN) {
            repMinFrontKneeAngle = repMinFrontKneeAngle?.let { min(it, frontKneeAngle) }
                ?: frontKneeAngle
            repMinBackKneeAngle =
                repMinBackKneeAngle?.let { min(it, backKneeAngle) } ?: backKneeAngle
            repMaxTorsoLeanAngle = repMaxTorsoLeanAngle?.let {
                max(it, counterResult.trunkTiltVerticalEma)
            } ?: counterResult.trunkTiltVerticalEma
            repMaxKneeForwardRatio =
                updateMaxRatio(repMaxKneeForwardRatio, frontKneeForwardRatio(metrics, leadLeg))
            repMaxKneeCollapseRatio =
                updateMaxRatio(repMaxKneeCollapseRatio, frontKneeCollapseRatio(metrics, leadLeg))
            updateStability(frame, metrics)
        }
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
        val stabilityStdDev = max(hipStats.standardDeviation(), shoulderStats.standardDeviation())
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

    private fun updateStability(frame: PoseFrame, metrics: LungeRawMetrics) {
        val width = frame.imageWidth.toFloat()
        if (width == LUNGE_FLOAT_ZERO) return
        hipStats.update(metrics.hipCenterX / width)
        shoulderStats.update(metrics.shoulderCenterX / width)
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
    }

    private fun frontKneeAngle(metrics: LungeRawMetrics, leadLeg: PoseSide): Float =
        if (leadLeg == PoseSide.LEFT) metrics.leftKneeAngle else metrics.rightKneeAngle

    private fun backKneeAngle(metrics: LungeRawMetrics, leadLeg: PoseSide): Float =
        if (leadLeg == PoseSide.LEFT) metrics.rightKneeAngle else metrics.leftKneeAngle

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

private data class LungeFeedbackResult(
    val repSummary: LungeRepSummary?,
    val bottomSummary: LungeRepSummary?
)

private data class FullBodyVisibilityState(
    val isVisible: Boolean,
    val invisibleDurationMs: Long,
    val feedbackEvent: PostureFeedbackType?
)
