package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REASON_DOWN_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REASON_UP_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ZERO
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
    val isReliable: Boolean,
    val transition: SquatPhaseTransition?,
    val upCandidateFrames: Int,
    val downCandidateFrames: Int
)

class LungeRepCounter(
    private val stateMachine: LungeStateMachine = LungeStateMachine(),
    private val kneeFilter: EmaFilter = EmaFilter(),
    private val trunkTiltFilter: EmaFilter = EmaFilter()
) {
    private var phase: SquatPhase = SquatPhase.UP
    private var repCount: Int = LUNGE_INT_ZERO
    private var upCandidateFrames: Int = LUNGE_INT_ZERO
    private var downCandidateFrames: Int = LUNGE_INT_ZERO
    private var lastKneeRaw: Float? = null
    private var lastTrunkTiltRaw: Float? = null

    fun update(
        timestampMs: Long,
        metrics: LungeRawMetrics?,
        frontKneeAngle: Float?
    ): LungeRepCounterResult? {
        if (frontKneeAngle != null) {
            lastKneeRaw = frontKneeAngle
        }
        if (metrics != null) {
            lastTrunkTiltRaw = metrics.trunkTiltVerticalAngle
        }
        val kneeAngleEma = frontKneeAngle?.let { kneeFilter.update(it) } ?: kneeFilter.current()
        val trunkTiltEma = metrics?.let { trunkTiltFilter.update(it.trunkTiltVerticalAngle) }
            ?: trunkTiltFilter.current()
        val kneeRaw = lastKneeRaw
        val trunkTiltRaw = lastTrunkTiltRaw ?: LUNGE_FLOAT_ZERO
        val trunkTiltEmaValue = trunkTiltEma ?: trunkTiltRaw
        if (kneeAngleEma == null || kneeRaw == null) {
            return null
        }
        val isReliable = frontKneeAngle != null
        val stateResult = stateMachine.update(kneeAngleEma, isReliable)
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
        }
        updateCandidateFrames(newPhase)
        return LungeRepCounterResult(
            repCount = repCount,
            phase = phase,
            state = stateResult.state,
            repCompleted = repCompleted,
            kneeAngleRaw = kneeRaw,
            kneeAngleEma = kneeAngleEma,
            trunkTiltVerticalRaw = trunkTiltRaw,
            trunkTiltVerticalEma = trunkTiltEmaValue,
            isReliable = isReliable,
            transition = transition,
            upCandidateFrames = upCandidateFrames,
            downCandidateFrames = downCandidateFrames
        )
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
