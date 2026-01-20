package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_ASCENDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_BOTTOM_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DESCENDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REP_COMPLETE_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STANDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STATE_HYSTERESIS_FRAMES
import com.jeong.runninggoaltracker.domain.model.SquatState

data class LungeStateMachineResult(
    val state: SquatState,
    val repCompleted: Boolean
)

class LungeStateMachine(
    private val hysteresisFrames: Int = LUNGE_STATE_HYSTERESIS_FRAMES,
    private val standingAngleThreshold: Float = LUNGE_STANDING_KNEE_ANGLE_THRESHOLD,
    private val descendingAngleThreshold: Float = LUNGE_DESCENDING_KNEE_ANGLE_THRESHOLD,
    private val bottomAngleThreshold: Float = LUNGE_BOTTOM_KNEE_ANGLE_THRESHOLD,
    private val ascendingAngleThreshold: Float = LUNGE_ASCENDING_KNEE_ANGLE_THRESHOLD,
    private val repCompleteAngleThreshold: Float = LUNGE_REP_COMPLETE_KNEE_ANGLE_THRESHOLD
) {
    private var state: SquatState = SquatState.STANDING
    private var candidateCount: Int = LUNGE_INT_ZERO

    fun update(kneeAngle: Float, isReliable: Boolean): LungeStateMachineResult {
        if (!isReliable) {
            return LungeStateMachineResult(state = state, repCompleted = false)
        }
        var repCompleted = false
        when (state) {
            SquatState.STANDING -> {
                applyTransition(kneeAngle <= descendingAngleThreshold, SquatState.DESCENDING)
            }

            SquatState.DESCENDING -> {
                when {
                    applyTransition(kneeAngle <= bottomAngleThreshold, SquatState.BOTTOM) -> Unit
                    applyTransition(
                        kneeAngle >= standingAngleThreshold,
                        SquatState.STANDING
                    ) -> Unit

                    else -> resetCandidate()
                }
            }

            SquatState.BOTTOM -> {
                applyTransition(kneeAngle >= ascendingAngleThreshold, SquatState.ASCENDING)
            }

            SquatState.ASCENDING -> {
                if (applyTransition(
                        kneeAngle >= repCompleteAngleThreshold,
                        SquatState.REP_COMPLETE
                    )
                ) {
                    repCompleted = true
                }
            }

            SquatState.REP_COMPLETE -> {
                when {
                    applyTransition(
                        kneeAngle >= standingAngleThreshold,
                        SquatState.STANDING
                    ) -> Unit

                    applyTransition(
                        kneeAngle <= descendingAngleThreshold,
                        SquatState.DESCENDING
                    ) -> Unit

                    else -> resetCandidate()
                }
            }
        }
        return LungeStateMachineResult(state = state, repCompleted = repCompleted)
    }

    private fun applyTransition(condition: Boolean, nextState: SquatState): Boolean {
        if (!condition) {
            resetCandidate()
            return false
        }
        candidateCount += LUNGE_INT_ONE
        if (candidateCount >= hysteresisFrames) {
            state = nextState
            resetCandidate()
            return true
        }
        return false
    }

    private fun resetCandidate() {
        candidateCount = LUNGE_INT_ZERO
    }
}
