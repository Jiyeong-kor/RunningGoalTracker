package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_BOTTOM_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_DOWN_FRAMES_REQUIRED
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_STANDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_UP_FRAMES_REQUIRED
import com.jeong.runninggoaltracker.domain.model.SquatPhase
import com.jeong.runninggoaltracker.domain.model.SquatPhaseTransition

data class SquatRepCounterResult(
    val repCount: Int,
    val phase: SquatPhase,
    val repCompleted: Boolean,
    val kneeAngle: Float,
    val trunkLeanAngle: Float,
    val isReliable: Boolean,
    val transition: SquatPhaseTransition?
)

class SquatRepCounter(
    private val upThreshold: Float = SQUAT_STANDING_KNEE_ANGLE_THRESHOLD,
    private val downThreshold: Float = SQUAT_BOTTOM_KNEE_ANGLE_THRESHOLD,
    private val upFramesRequired: Int = SQUAT_UP_FRAMES_REQUIRED,
    private val downFramesRequired: Int = SQUAT_DOWN_FRAMES_REQUIRED
) {
    private val kneeFilter = EmaFilter()
    private val trunkFilter = EmaFilter()
    private var phase: SquatPhase = SquatPhase.UP
    private var repCount: Int = SQUAT_INT_ZERO
    private var upCandidateFrames: Int = SQUAT_INT_ZERO
    private var downCandidateFrames: Int = SQUAT_INT_ZERO

    fun update(
        timestampMs: Long,
        metrics: PoseRawSquatMetrics?,
        isReliable: Boolean
    ): SquatRepCounterResult? {
        val kneeAngle = metrics?.let { kneeFilter.update(it.kneeAngle) } ?: kneeFilter.current()
        val trunkLean =
            metrics?.let { trunkFilter.update(it.trunkLeanAngle) } ?: trunkFilter.current()
        if (kneeAngle == null || trunkLean == null) return null
        var transition: SquatPhaseTransition? = null
        var repCompleted = false
        if (isReliable) {
            when (phase) {
                SquatPhase.UP -> {
                    if (kneeAngle <= downThreshold) {
                        downCandidateFrames += SQUAT_INT_ONE
                        if (downCandidateFrames >= downFramesRequired) {
                            transition = SquatPhaseTransition(
                                from = phase,
                                to = SquatPhase.DOWN,
                                timestampMs = timestampMs
                            )
                            phase = SquatPhase.DOWN
                            resetDownCandidate()
                        }
                    } else {
                        resetDownCandidate()
                    }
                }

                SquatPhase.DOWN -> {
                    if (kneeAngle >= upThreshold) {
                        upCandidateFrames += SQUAT_INT_ONE
                        if (upCandidateFrames >= upFramesRequired) {
                            transition = SquatPhaseTransition(
                                from = phase,
                                to = SquatPhase.UP,
                                timestampMs = timestampMs
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
            kneeAngle = kneeAngle,
            trunkLeanAngle = trunkLean,
            isReliable = isReliable,
            transition = transition
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
