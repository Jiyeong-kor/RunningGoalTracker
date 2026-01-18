package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_BOTTOM_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_DOWN_FRAMES_REQUIRED
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_REASON_DOWN_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_REASON_UP_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_STANDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_UP_FRAMES_REQUIRED
import com.jeong.runninggoaltracker.domain.model.SquatPhase
import com.jeong.runninggoaltracker.domain.model.SquatPhaseTransition


data class SquatRepCounterResult(
    val repCount: Int,
    val phase: SquatPhase,
    val repCompleted: Boolean,
    val kneeAngleRaw: Float,
    val kneeAngleEma: Float,
    val trunkTiltVerticalRaw: Float,
    val trunkTiltVerticalEma: Float,
    val trunkToThighRaw: Float,
    val trunkToThighEma: Float,
    val isReliable: Boolean,
    val transition: SquatPhaseTransition?,
    val upCandidateFrames: Int,
    val downCandidateFrames: Int
)

class SquatRepCounter(
    private val upThreshold: Float = SQUAT_STANDING_KNEE_ANGLE_THRESHOLD,
    private val downThreshold: Float = SQUAT_BOTTOM_KNEE_ANGLE_THRESHOLD,
    private val upFramesRequired: Int = SQUAT_UP_FRAMES_REQUIRED,
    private val downFramesRequired: Int = SQUAT_DOWN_FRAMES_REQUIRED
) {
    private val kneeFilter = EmaFilter()
    private val trunkTiltFilter = EmaFilter()
    private val trunkToThighFilter = EmaFilter()
    private var phase: SquatPhase = SquatPhase.UP
    private var repCount: Int = SQUAT_INT_ZERO
    private var upCandidateFrames: Int = SQUAT_INT_ZERO
    private var downCandidateFrames: Int = SQUAT_INT_ZERO
    private var lastKneeRaw: Float? = null
    private var lastTrunkTiltRaw: Float? = null
    private var lastTrunkToThighRaw: Float? = null

    fun update(timestampMs: Long, metrics: PoseRawSquatMetrics?): SquatRepCounterResult? {
        if (metrics != null) {
            lastKneeRaw = metrics.kneeAngle
            lastTrunkTiltRaw = metrics.trunkTiltVerticalAngle
            lastTrunkToThighRaw = metrics.trunkToThighAngle
        }
        val kneeAngleEma = metrics?.let { kneeFilter.update(it.kneeAngle) } ?: kneeFilter.current()
        val trunkTiltVerticalEma =
            metrics?.let { trunkTiltFilter.update(it.trunkTiltVerticalAngle) }
                ?: trunkTiltFilter.current()
        val trunkToThighEma =
            metrics?.let { trunkToThighFilter.update(it.trunkToThighAngle) }
                ?: trunkToThighFilter.current()
        val kneeRaw = lastKneeRaw
        val trunkTiltRaw = lastTrunkTiltRaw
        val trunkToThighRaw = lastTrunkToThighRaw
        if (kneeAngleEma == null ||
            trunkTiltVerticalEma == null ||
            trunkToThighEma == null ||
            kneeRaw == null ||
            trunkTiltRaw == null ||
            trunkToThighRaw == null
        ) {
            return null
        }
        var transition: SquatPhaseTransition? = null
        var repCompleted = false
        val isReliable = metrics != null
        if (isReliable) {
            when (phase) {
                SquatPhase.UP -> {
                    if (kneeAngleEma <= downThreshold) {
                        downCandidateFrames += SQUAT_INT_ONE
                        if (downCandidateFrames >= downFramesRequired) {
                            transition = SquatPhaseTransition(
                                from = phase,
                                to = SquatPhase.DOWN,
                                timestampMs = timestampMs,
                                reason = SQUAT_REASON_DOWN_THRESHOLD
                            )
                            phase = SquatPhase.DOWN
                            resetDownCandidate()
                        }
                    } else {
                        resetDownCandidate()
                    }
                }

                SquatPhase.DOWN -> {
                    if (kneeAngleEma >= upThreshold) {
                        upCandidateFrames += SQUAT_INT_ONE
                        if (upCandidateFrames >= upFramesRequired) {
                            transition = SquatPhaseTransition(
                                from = phase,
                                to = SquatPhase.UP,
                                timestampMs = timestampMs,
                                reason = SQUAT_REASON_UP_THRESHOLD
                            )
                            phase = SquatPhase.UP
                            repCount += SQUAT_INT_ONE
                            repCompleted = true
                            resetUpCandidate()
                        }
                    } else {
                        resetUpCandidate()
                    }
                }
            }
        } else {
            resetCandidates()
        }
        return SquatRepCounterResult(
            repCount = repCount,
            phase = phase,
            repCompleted = repCompleted,
            kneeAngleRaw = kneeRaw,
            kneeAngleEma = kneeAngleEma,
            trunkTiltVerticalRaw = trunkTiltRaw,
            trunkTiltVerticalEma = trunkTiltVerticalEma,
            trunkToThighRaw = trunkToThighRaw,
            trunkToThighEma = trunkToThighEma,
            isReliable = isReliable,
            transition = transition,
            upCandidateFrames = upCandidateFrames,
            downCandidateFrames = downCandidateFrames
        )
    }

    fun upThreshold(): Float = upThreshold

    fun downThreshold(): Float = downThreshold

    fun upFramesRequired(): Int = upFramesRequired

    fun downFramesRequired(): Int = downFramesRequired

    private fun resetUpCandidate() = Unit.also { upCandidateFrames = SQUAT_INT_ZERO }

    private fun resetDownCandidate() = Unit.also { downCandidateFrames = SQUAT_INT_ZERO }

    private fun resetCandidates() = Unit.also {
        resetUpCandidate()
        resetDownCandidate()
    }
}
