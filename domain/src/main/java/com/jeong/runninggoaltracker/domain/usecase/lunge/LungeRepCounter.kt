package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REASON_DOWN_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REASON_UP_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_COUNTING_SIDE_LOCK_FRAMES
import com.jeong.runninggoaltracker.domain.contract.LUNGE_RELIABILITY_GRACE_MS
import com.jeong.runninggoaltracker.domain.model.PoseSide
import com.jeong.runninggoaltracker.domain.model.SquatPhase
import com.jeong.runninggoaltracker.domain.model.SquatPhaseTransition
import com.jeong.runninggoaltracker.domain.model.SquatState
import com.jeong.runninggoaltracker.domain.usecase.squat.EmaFilter

data class LungeRepCounterResult(
    val repCount: Int,
    val phase: SquatPhase,
    val state: SquatState,
    val repCompleted: Boolean,
    val kneeAngleRaw: Float,
    val kneeAngleEma: Float,
    val trunkTiltVerticalRaw: Float,
    val trunkTiltVerticalEma: Float,
    val countingSide: PoseSide?,
    val countingKneeAngle: Float?,
    val isReliable: Boolean,
    val transition: SquatPhaseTransition?,
    val upCandidateFrames: Int,
    val downCandidateFrames: Int,
    val standingToDescendingCount: Int,
    val descendingToBottomCount: Int,
    val descendingToStandingCount: Int,
    val bottomToAscendingCount: Int,
    val ascendingToCompleteCount: Int,
    val repCompleteToStandingCount: Int,
    val repCompleteToDescendingCount: Int,
    val hysteresisFrames: Int
)

class LungeRepCounter(
    private val kneeFilter: EmaFilter = EmaFilter(),
    private val trunkTiltFilter: EmaFilter = EmaFilter(),
    private val debugLogger: (Any) -> Unit = {}
) {
    private val stateMachine = LungeStateMachine(debugLogger = debugLogger)
    private var phase: SquatPhase = SquatPhase.UP
    private var repCount: Int = LUNGE_INT_ZERO
    private var upCandidateFrames: Int = LUNGE_INT_ZERO
    private var downCandidateFrames: Int = LUNGE_INT_ZERO
    private var lastKneeRaw: Float? = null
    private var lastTrunkTiltRaw: Float? = null
    private var lastLeftValidKneeRaw: Float? = null
    private var lastRightValidKneeRaw: Float? = null
    private var countingSide: PoseSide? = null
    private var lockFramesRemaining: Int = LUNGE_INT_ZERO
    private var leftLockScore: Float = LUNGE_FLOAT_ZERO
    private var rightLockScore: Float = LUNGE_FLOAT_ZERO
    private var lastReliableTimestampMs: Long? = null
    private var lastState: SquatState? = null
    private var lastPhase: SquatPhase? = null
    private var lastIsReliable: Boolean? = null
    private val sanitizer = LungeKneeAngleSanitizer()

    fun update(
        timestampMs: Long,
        metrics: LungeRawMetrics?,
        leftKneeAngle: Float?,
        rightKneeAngle: Float?,
        leadLeg: PoseSide?
    ): LungeRepCounterResult? {
        val previousLeftValid = lastLeftValidKneeRaw
        val previousRightValid = lastRightValidKneeRaw
        val leftAngleResult = sanitizer.sanitize(leftKneeAngle, lastLeftValidKneeRaw, PoseSide.LEFT)
        val rightAngleResult = sanitizer.sanitize(rightKneeAngle, lastRightValidKneeRaw, PoseSide.RIGHT)
        val leftValidAngle = leftAngleResult.angle
        val rightValidAngle = rightAngleResult.angle
        if (leftValidAngle != null) {
            lastLeftValidKneeRaw = leftValidAngle
        }
        if (rightValidAngle != null) {
            lastRightValidKneeRaw = rightValidAngle
        }
        if (metrics != null) {
            lastTrunkTiltRaw = metrics.trunkTiltVerticalAngle
        }
        val stateResultPre = lastState
        val withinGrace = isWithinReliabilityGrace(timestampMs)
        val leftAngleForCounting = leftValidAngle ?: if (withinGrace) lastLeftValidKneeRaw else null
        val rightAngleForCounting = rightValidAngle ?: if (withinGrace) lastRightValidKneeRaw else null
        val countingAngle =
            selectCountingAngle(countingSide, leadLeg, leftAngleForCounting, rightAngleForCounting)
        if (countingAngle != null) {
            lastKneeRaw = countingAngle
            lastReliableTimestampMs = timestampMs
        }
        val kneeAngleEma = countingAngle?.let { kneeFilter.update(it) } ?: kneeFilter.current()
        val trunkTiltEma = metrics?.let { trunkTiltFilter.update(it.trunkTiltVerticalAngle) }
            ?: trunkTiltFilter.current()
        val kneeRaw = lastKneeRaw
        val trunkTiltRaw = lastTrunkTiltRaw ?: LUNGE_FLOAT_ZERO
        val trunkTiltEmaValue = trunkTiltEma ?: trunkTiltRaw
        if (kneeAngleEma == null || kneeRaw == null) {
            return null
        }
        val isReliable = countingAngle != null || withinGrace
        val stateResult = stateMachine.update(kneeAngleEma, kneeRaw, isReliable)
        if (stateResult.state == SquatState.DESCENDING && stateResultPre == SquatState.STANDING) {
            startCountingSideLock()
        }
        if (lockFramesRemaining > LUNGE_INT_ZERO) {
            updateCountingSideLock(
                previousLeft = previousLeftValid,
                currentLeft = leftValidAngle,
                previousRight = previousRightValid,
                currentRight = rightValidAngle,
                leadLeg = leadLeg
            )
        }
        val newPhase = if (stateResult.state == SquatState.DESCENDING ||
            stateResult.state == SquatState.BOTTOM
        ) {
            SquatPhase.DOWN
        } else {
            SquatPhase.UP
        }
        var transition: SquatPhaseTransition? = null
        if (newPhase != phase && isReliable) {
            transition = SquatPhaseTransition(
                from = phase,
                to = newPhase,
                timestampMs = timestampMs,
                reason = if (newPhase == SquatPhase.DOWN) {
                    LUNGE_REASON_DOWN_THRESHOLD
                } else {
                    LUNGE_REASON_UP_THRESHOLD
                }
            )
            phase = newPhase
        }
        val repCompleted = stateResult.repCompleted
        if (repCompleted) {
            repCount += LUNGE_INT_ONE
            countingSide = null
            lastLeftValidKneeRaw = null
            lastRightValidKneeRaw = null
            lockFramesRemaining = LUNGE_INT_ZERO
            leftLockScore = LUNGE_FLOAT_ZERO
            rightLockScore = LUNGE_FLOAT_ZERO
        }
        updateCandidateFrames(newPhase)
        val outlier = leftAngleResult.outlier ?: rightAngleResult.outlier
        val shouldLog = stateResult.state != lastState ||
            phase != lastPhase ||
            isReliable != lastIsReliable ||
            transition != null ||
            outlier != null
        if (shouldLog) {
            debugLogger(
                LungeRepCounterDebug(
                    timestampMs = timestampMs,
                    state = stateResult.state,
                    phase = phase,
                    isReliable = isReliable,
                    kneeAngleRaw = kneeRaw,
                    kneeAngleEma = kneeAngleEma,
                    transition = transition,
                    countingSide = countingSide,
                    leftKneeAngle = leftValidAngle,
                    rightKneeAngle = rightValidAngle,
                    countingKneeAngle = countingAngle,
                    outlier = outlier,
                    leftRawKneeAngle = leftKneeAngle,
                    rightRawKneeAngle = rightKneeAngle,
                    leftOutlier = leftAngleResult.outlier,
                    rightOutlier = rightAngleResult.outlier,
                    lockFramesRemaining = lockFramesRemaining,
                    repCompleted = repCompleted,
                    repCount = repCount
                )
            )
        }
        lastState = stateResult.state
        lastPhase = phase
        lastIsReliable = isReliable
        return LungeRepCounterResult(
            repCount = repCount,
            phase = phase,
            state = stateResult.state,
            repCompleted = repCompleted,
            kneeAngleRaw = kneeRaw,
            kneeAngleEma = kneeAngleEma,
            trunkTiltVerticalRaw = trunkTiltRaw,
            trunkTiltVerticalEma = trunkTiltEmaValue,
            countingSide = countingSide,
            countingKneeAngle = countingAngle,
            isReliable = isReliable,
            transition = transition,
            upCandidateFrames = upCandidateFrames,
            downCandidateFrames = downCandidateFrames,
            standingToDescendingCount = stateResult.standingToDescendingCount,
            descendingToBottomCount = stateResult.descendingToBottomCount,
            descendingToStandingCount = stateResult.descendingToStandingCount,
            bottomToAscendingCount = stateResult.bottomToAscendingCount,
            ascendingToCompleteCount = stateResult.ascendingToCompleteCount,
            repCompleteToStandingCount = stateResult.repCompleteToStandingCount,
            repCompleteToDescendingCount = stateResult.repCompleteToDescendingCount,
            hysteresisFrames = stateResult.hysteresisFrames
        )
    }

    private fun isWithinReliabilityGrace(timestampMs: Long): Boolean =
        lastReliableTimestampMs?.let { timestampMs - it <= LUNGE_RELIABILITY_GRACE_MS } ?: false

    private fun selectCountingAngle(
        currentSide: PoseSide?,
        leadLeg: PoseSide?,
        leftKneeAngle: Float?,
        rightKneeAngle: Float?
    ): Float? = when (currentSide ?: leadLeg) {
        PoseSide.LEFT -> leftKneeAngle
        PoseSide.RIGHT -> rightKneeAngle
        null -> leftKneeAngle ?: rightKneeAngle
    }

    private fun startCountingSideLock() {
        lockFramesRemaining = LUNGE_COUNTING_SIDE_LOCK_FRAMES
        leftLockScore = LUNGE_FLOAT_ZERO
        rightLockScore = LUNGE_FLOAT_ZERO
        countingSide = null
    }

    private fun updateCountingSideLock(
        previousLeft: Float?,
        currentLeft: Float?,
        previousRight: Float?,
        currentRight: Float?,
        leadLeg: PoseSide?
    ) {
        if (currentLeft != null && previousLeft != null) {
            leftLockScore += (previousLeft - currentLeft).coerceAtLeast(LUNGE_FLOAT_ZERO)
        }
        if (currentRight != null && previousRight != null) {
            rightLockScore += (previousRight - currentRight).coerceAtLeast(LUNGE_FLOAT_ZERO)
        }
        lockFramesRemaining -= LUNGE_INT_ONE
        if (lockFramesRemaining == LUNGE_INT_ZERO && countingSide == null) {
            countingSide = when {
                leftLockScore > rightLockScore -> PoseSide.LEFT
                rightLockScore > leftLockScore -> PoseSide.RIGHT
                currentLeft != null && currentRight != null && currentLeft < currentRight -> PoseSide.LEFT
                currentLeft != null && currentRight != null && currentRight < currentLeft -> PoseSide.RIGHT
                currentLeft != null && currentRight == null -> PoseSide.LEFT
                currentRight != null && currentLeft == null -> PoseSide.RIGHT
                else -> leadLeg
            }
        }
    }

    private fun updateCandidateFrames(phase: SquatPhase) {
        if (phase == SquatPhase.UP) {
            upCandidateFrames += LUNGE_INT_ONE
            downCandidateFrames = LUNGE_INT_ZERO
        } else {
            downCandidateFrames += LUNGE_INT_ONE
            upCandidateFrames = LUNGE_INT_ZERO
        }
    }
}

private data class LungeRepCounterDebug(
    val timestampMs: Long,
    val state: SquatState,
    val phase: SquatPhase,
    val isReliable: Boolean,
    val kneeAngleRaw: Float,
    val kneeAngleEma: Float,
    val transition: SquatPhaseTransition?,
    val countingSide: PoseSide?,
    val leftKneeAngle: Float?,
    val rightKneeAngle: Float?,
    val countingKneeAngle: Float?,
    val outlier: LungeKneeAngleOutlier?,
    val leftRawKneeAngle: Float?,
    val rightRawKneeAngle: Float?,
    val leftOutlier: LungeKneeAngleOutlier?,
    val rightOutlier: LungeKneeAngleOutlier?,
    val lockFramesRemaining: Int,
    val repCompleted: Boolean,
    val repCount: Int
)
