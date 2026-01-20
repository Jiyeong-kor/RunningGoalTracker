package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_LEAD_LEG_WINDOW
import com.jeong.runninggoaltracker.domain.model.PoseSide
import java.util.ArrayDeque

class LungeLeadLegSelector(
    private val windowSize: Int = LUNGE_LEAD_LEG_WINDOW
) {
    private val history = ArrayDeque<PoseSide>()
    private var lastSelection: PoseSide? = null

    fun update(metrics: LungeRawMetrics?): PoseSide? {
        val currentSelection = metrics?.let { leadLeg(it) } ?: return lastSelection
        history.addLast(currentSelection)
        if (history.size > windowSize) {
            history.removeFirst()
        }
        val leftCount = history.count { it == PoseSide.LEFT }
        val rightCount = history.size - leftCount
        val selected = when {
            leftCount > rightCount -> PoseSide.LEFT
            rightCount > leftCount -> PoseSide.RIGHT
            else -> lastSelection ?: currentSelection
        }
        lastSelection = selected
        return selected
    }

    private fun leadLeg(metrics: LungeRawMetrics): PoseSide {
        val leftKnee = metrics.leftKneeAngle
        val rightKnee = metrics.rightKneeAngle
        return if (leftKnee < rightKnee) {
            PoseSide.LEFT
        } else if (rightKnee < leftKnee) {
            PoseSide.RIGHT
        } else {
            lastSelection ?: PoseSide.LEFT
        }
    }
}
