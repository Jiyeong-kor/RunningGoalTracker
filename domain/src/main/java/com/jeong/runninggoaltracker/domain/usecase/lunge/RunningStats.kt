package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_ZERO
import kotlin.math.sqrt

class RunningStats {
    private var count: Int = LUNGE_INT_ZERO
    private var mean: Float = LUNGE_FLOAT_ZERO
    private var sumSquaredDiff: Float = LUNGE_FLOAT_ZERO

    fun update(value: Float) {
        count += LUNGE_INT_ONE
        val delta = value - mean
        mean += delta / count
        val delta2 = value - mean
        sumSquaredDiff += delta * delta2
    }

    fun standardDeviation(): Float =
        if (count <= LUNGE_INT_ONE) {
            LUNGE_FLOAT_ZERO
        } else {
            sqrt(sumSquaredDiff / (count - LUNGE_INT_ONE))
        }

    fun reset() {
        count = LUNGE_INT_ZERO
        mean = LUNGE_FLOAT_ZERO
        sumSquaredDiff = LUNGE_FLOAT_ZERO
    }

    fun sampleCount(): Int = count
}
