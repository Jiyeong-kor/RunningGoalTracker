package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_ASCENDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_BOTTOM_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DESCENDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_CANDIDATE_DECAY_STEP
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REP_COMPLETE_MARGIN
import com.jeong.runninggoaltracker.domain.contract.LUNGE_REP_COMPLETE_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STANDING_KNEE_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STATE_HYSTERESIS_FRAMES
import com.jeong.runninggoaltracker.domain.model.SquatState

data class LungeStateMachineResult(
    val state: SquatState,
    val repCompleted: Boolean,
    val standingToDescendingCount: Int,
    val descendingToBottomCount: Int,
    val descendingToStandingCount: Int,
    val bottomToAscendingCount: Int,
    val ascendingToCompleteCount: Int,
    val repCompleteToStandingCount: Int,
    val repCompleteToDescendingCount: Int,
    val hysteresisFrames: Int
)

class LungeStateMachine(
    private val hysteresisFrames: Int = LUNGE_STATE_HYSTERESIS_FRAMES,
    private val standingAngleThreshold: Float = LUNGE_STANDING_KNEE_ANGLE_THRESHOLD,
    private val descendingAngleThreshold: Float = LUNGE_DESCENDING_KNEE_ANGLE_THRESHOLD,
    private val bottomAngleThreshold: Float = LUNGE_BOTTOM_KNEE_ANGLE_THRESHOLD,
    private val ascendingAngleThreshold: Float = LUNGE_ASCENDING_KNEE_ANGLE_THRESHOLD,
    private val repCompleteAngleThreshold: Float = LUNGE_REP_COMPLETE_KNEE_ANGLE_THRESHOLD,
    private val repCompleteMargin: Float = LUNGE_REP_COMPLETE_MARGIN,
    private val debugLogger: (Any) -> Unit = {}
) {
    private var state: SquatState = SquatState.STANDING
    private var standingToDescendingCount: Int = LUNGE_INT_ZERO
    private var descendingToBottomCount: Int = LUNGE_INT_ZERO
    private var descendingToStandingCount: Int = LUNGE_INT_ZERO
    private var bottomToAscendingCount: Int = LUNGE_INT_ZERO
    private var ascendingToCompleteCount: Int = LUNGE_INT_ZERO
    private var repCompleteToStandingCount: Int = LUNGE_INT_ZERO
    private var repCompleteToDescendingCount: Int = LUNGE_INT_ZERO

    fun update(kneeAngleEma: Float, kneeAngleRaw: Float, isReliable: Boolean): LungeStateMachineResult {
        if (!isReliable) {
            return LungeStateMachineResult(
                state = state,
                repCompleted = false,
                standingToDescendingCount = standingToDescendingCount,
                descendingToBottomCount = descendingToBottomCount,
                descendingToStandingCount = descendingToStandingCount,
                bottomToAscendingCount = bottomToAscendingCount,
                ascendingToCompleteCount = ascendingToCompleteCount,
                repCompleteToStandingCount = repCompleteToStandingCount,
                repCompleteToDescendingCount = repCompleteToDescendingCount,
                hysteresisFrames = hysteresisFrames
            )
        }
        val descentAngle = minOf(kneeAngleEma, kneeAngleRaw)
        var repCompleted = false
        when (state) {
            SquatState.STANDING -> {
                if (applyTransition(
                        condition = descentAngle <= descendingAngleThreshold,
                        currentCount = standingToDescendingCount,
                        nextState = SquatState.DESCENDING,
                        threshold = descendingAngleThreshold,
                        angleEma = kneeAngleEma,
                        angleRaw = kneeAngleRaw
                    )
                ) {
                    standingToDescendingCount = LUNGE_INT_ZERO
                } else {
                    standingToDescendingCount = updateCount(
                        standingToDescendingCount,
                        descentAngle <= descendingAngleThreshold,
                        allowDecay = false
                    )
                }
            }

            SquatState.DESCENDING -> {
                if (applyTransition(
                        condition = descentAngle <= bottomAngleThreshold,
                        currentCount = descendingToBottomCount,
                        nextState = SquatState.BOTTOM,
                        threshold = bottomAngleThreshold,
                        angleEma = kneeAngleEma,
                        angleRaw = kneeAngleRaw
                    )
                ) {
                    descendingToBottomCount = LUNGE_INT_ZERO
                } else {
                    descendingToBottomCount = updateCount(
                        descendingToBottomCount,
                        descentAngle <= bottomAngleThreshold,
                        allowDecay = false
                    )
                }
                if (state == SquatState.DESCENDING) {
                    if (applyTransition(
                            condition = kneeAngleEma >= standingAngleThreshold,
                            currentCount = descendingToStandingCount,
                            nextState = SquatState.STANDING,
                            threshold = standingAngleThreshold,
                            angleEma = kneeAngleEma,
                            angleRaw = kneeAngleRaw
                        )
                    ) {
                        descendingToStandingCount = LUNGE_INT_ZERO
                    } else {
                        descendingToStandingCount = updateCount(
                            descendingToStandingCount,
                            kneeAngleEma >= standingAngleThreshold,
                            allowDecay = false
                        )
                    }
                }
            }

            SquatState.BOTTOM -> {
                if (applyTransition(
                        condition = kneeAngleEma >= ascendingAngleThreshold,
                        currentCount = bottomToAscendingCount,
                        nextState = SquatState.ASCENDING,
                        threshold = ascendingAngleThreshold,
                        angleEma = kneeAngleEma,
                        angleRaw = kneeAngleRaw
                    )
                ) {
                    bottomToAscendingCount = LUNGE_INT_ZERO
                } else {
                    bottomToAscendingCount = updateCount(
                        bottomToAscendingCount,
                        kneeAngleEma >= ascendingAngleThreshold,
                        allowDecay = false
                    )
                }
            }

            SquatState.ASCENDING -> {
                if (applyTransition(
                        condition = kneeAngleRaw >= repCompleteAngleThreshold - repCompleteMargin,
                        currentCount = ascendingToCompleteCount,
                        nextState = SquatState.REP_COMPLETE,
                        threshold = repCompleteAngleThreshold - repCompleteMargin,
                        angleEma = kneeAngleEma,
                        angleRaw = kneeAngleRaw
                    )
                ) {
                    repCompleted = true
                    ascendingToCompleteCount = LUNGE_INT_ZERO
                } else {
                    ascendingToCompleteCount = updateCount(
                        ascendingToCompleteCount,
                        kneeAngleRaw >= repCompleteAngleThreshold - repCompleteMargin,
                        allowDecay = true
                    )
                }
            }

            SquatState.REP_COMPLETE -> {
                if (applyTransition(
                        condition = kneeAngleRaw >= standingAngleThreshold,
                        currentCount = repCompleteToStandingCount,
                        nextState = SquatState.STANDING,
                        threshold = standingAngleThreshold,
                        angleEma = kneeAngleEma,
                        angleRaw = kneeAngleRaw
                    )
                ) {
                    repCompleteToStandingCount = LUNGE_INT_ZERO
                } else {
                    repCompleteToStandingCount = updateCount(
                        repCompleteToStandingCount,
                        kneeAngleRaw >= standingAngleThreshold,
                        allowDecay = true
                    )
                }
                if (state == SquatState.REP_COMPLETE) {
                    if (applyTransition(
                            condition = kneeAngleEma <= descendingAngleThreshold,
                            currentCount = repCompleteToDescendingCount,
                            nextState = SquatState.DESCENDING,
                            threshold = descendingAngleThreshold,
                            angleEma = kneeAngleEma,
                            angleRaw = kneeAngleRaw
                        )
                    ) {
                        repCompleteToDescendingCount = LUNGE_INT_ZERO
                    } else {
                        repCompleteToDescendingCount = updateCount(
                            repCompleteToDescendingCount,
                            kneeAngleEma <= descendingAngleThreshold,
                            allowDecay = false
                        )
                    }
                }
            }
        }
        debugLogger(
            LungeStateMachineDebug(
                state = state,
                kneeAngleEma = kneeAngleEma,
                kneeAngleRaw = kneeAngleRaw,
                standingThreshold = standingAngleThreshold,
                descendingThreshold = descendingAngleThreshold,
                bottomThreshold = bottomAngleThreshold,
                ascendingThreshold = ascendingAngleThreshold,
                repCompleteThreshold = repCompleteAngleThreshold - repCompleteMargin,
                standingToDescendingCount = standingToDescendingCount,
                descendingToBottomCount = descendingToBottomCount,
                descendingToStandingCount = descendingToStandingCount,
                bottomToAscendingCount = bottomToAscendingCount,
                ascendingToCompleteCount = ascendingToCompleteCount,
                repCompleteToStandingCount = repCompleteToStandingCount,
                repCompleteToDescendingCount = repCompleteToDescendingCount,
                repCompleted = repCompleted
            )
        )
        return LungeStateMachineResult(
            state = state,
            repCompleted = repCompleted,
            standingToDescendingCount = standingToDescendingCount,
            descendingToBottomCount = descendingToBottomCount,
            descendingToStandingCount = descendingToStandingCount,
            bottomToAscendingCount = bottomToAscendingCount,
            ascendingToCompleteCount = ascendingToCompleteCount,
            repCompleteToStandingCount = repCompleteToStandingCount,
            repCompleteToDescendingCount = repCompleteToDescendingCount,
            hysteresisFrames = hysteresisFrames
        )
    }

    private fun applyTransition(
        condition: Boolean,
        currentCount: Int,
        nextState: SquatState,
        threshold: Float,
        angleEma: Float,
        angleRaw: Float
    ): Boolean {
        if (!condition) {
            debugLogger(
                LungeStateMachineTransitionDebug(
                    currentState = state,
                    nextState = nextState,
                    conditionMet = false,
                    candidateCount = currentCount,
                    threshold = threshold,
                    kneeAngleEma = angleEma,
                    kneeAngleRaw = angleRaw
                )
            )
            return false
        }
        val updated = currentCount + LUNGE_INT_ONE
        debugLogger(
            LungeStateMachineTransitionDebug(
                currentState = state,
                nextState = nextState,
                conditionMet = true,
                candidateCount = updated,
                threshold = threshold,
                kneeAngleEma = angleEma,
                kneeAngleRaw = angleRaw
            )
        )
        if (updated >= hysteresisFrames) {
            state = nextState
            debugLogger(
                LungeStateMachineStateChangeDebug(
                    state = state,
                    kneeAngleEma = angleEma,
                    kneeAngleRaw = angleRaw
                )
            )
            return true
        }
        return false
    }

    private fun updateCount(currentCount: Int, condition: Boolean, allowDecay: Boolean): Int =
        if (condition) {
            (currentCount + LUNGE_INT_ONE).coerceAtMost(hysteresisFrames)
        } else if (allowDecay) {
            (currentCount - LUNGE_CANDIDATE_DECAY_STEP).coerceAtLeast(LUNGE_INT_ZERO)
        } else {
            LUNGE_INT_ZERO
        }
}

private data class LungeStateMachineDebug(
    val state: SquatState,
    val kneeAngleEma: Float,
    val kneeAngleRaw: Float,
    val standingThreshold: Float,
    val descendingThreshold: Float,
    val bottomThreshold: Float,
    val ascendingThreshold: Float,
    val repCompleteThreshold: Float,
    val standingToDescendingCount: Int,
    val descendingToBottomCount: Int,
    val descendingToStandingCount: Int,
    val bottomToAscendingCount: Int,
    val ascendingToCompleteCount: Int,
    val repCompleteToStandingCount: Int,
    val repCompleteToDescendingCount: Int,
    val repCompleted: Boolean
)

private data class LungeStateMachineTransitionDebug(
    val currentState: SquatState,
    val nextState: SquatState,
    val conditionMet: Boolean,
    val candidateCount: Int,
    val threshold: Float,
    val kneeAngleEma: Float,
    val kneeAngleRaw: Float
)

private data class LungeStateMachineStateChangeDebug(
    val state: SquatState,
    val kneeAngleEma: Float,
    val kneeAngleRaw: Float
)
