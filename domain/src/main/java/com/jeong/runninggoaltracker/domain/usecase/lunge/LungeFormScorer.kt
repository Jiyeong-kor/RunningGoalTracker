package com.jeong.runninggoaltracker.domain.usecase.lunge

import com.jeong.runninggoaltracker.domain.contract.LUNGE_BACK_KNEE_MAX_DEVIATION
import com.jeong.runninggoaltracker.domain.contract.LUNGE_BACK_KNEE_TARGET_MAX_ANGLE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_BACK_KNEE_TARGET_MIN_ANGLE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_DEEP_FRONT
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_SHALLOW_BACK
import com.jeong.runninggoaltracker.domain.contract.LUNGE_DEPTH_TOO_SHALLOW_FRONT
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FRONT_KNEE_MAX_DEVIATION
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FRONT_KNEE_TARGET_MAX_ANGLE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_FRONT_KNEE_TARGET_MIN_ANGLE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_INT_TWO
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_COLLAPSE_INWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_COLLAPSE_RATIO_HARD_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_COLLAPSE_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_FORWARD_RATIO_HARD_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_FORWARD_RATIO_SOFT_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_KNEE_TOO_FORWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_SCORE_MAX
import com.jeong.runninggoaltracker.domain.contract.LUNGE_SCORE_WEIGHT_ALIGNMENT
import com.jeong.runninggoaltracker.domain.contract.LUNGE_SCORE_WEIGHT_DEPTH
import com.jeong.runninggoaltracker.domain.contract.LUNGE_SCORE_WEIGHT_POSTURE
import com.jeong.runninggoaltracker.domain.contract.LUNGE_SCORE_WEIGHT_STABILITY
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STABILITY_SWAY_HARD_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_STABILITY_SWAY_SOFT_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_TORSO_LEAN_HARD_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_TORSO_LEAN_SOFT_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_TORSO_TOO_LEAN_FORWARD
import com.jeong.runninggoaltracker.domain.contract.LUNGE_UNSTABLE
import com.jeong.runninggoaltracker.domain.model.LungeRepSummary
import com.jeong.runninggoaltracker.domain.model.PoseSide
import kotlin.math.roundToInt

class LungeFormScorer {
    fun score(metrics: LungeRepMetrics): LungeRepSummary {
        val frontDepthPenalty = rangePenalty(
            value = metrics.frontKneeMinAngle,
            min = LUNGE_FRONT_KNEE_TARGET_MIN_ANGLE,
            max = LUNGE_FRONT_KNEE_TARGET_MAX_ANGLE,
            maxDeviation = LUNGE_FRONT_KNEE_MAX_DEVIATION
        )
        val backDepthPenalty = rangePenalty(
            value = metrics.backKneeMinAngle,
            min = LUNGE_BACK_KNEE_TARGET_MIN_ANGLE,
            max = LUNGE_BACK_KNEE_TARGET_MAX_ANGLE,
            maxDeviation = LUNGE_BACK_KNEE_MAX_DEVIATION
        )
        val depthPenalty = ((frontDepthPenalty + backDepthPenalty) / LUNGE_INT_TWO.toFloat())
            .coerceIn(LUNGE_FLOAT_ZERO, LUNGE_FLOAT_ONE)
        val kneeForwardPenalty = thresholdPenalty(
            value = metrics.maxKneeForwardRatio,
            soft = LUNGE_KNEE_FORWARD_RATIO_SOFT_THRESHOLD,
            hard = LUNGE_KNEE_FORWARD_RATIO_HARD_THRESHOLD
        )
        val kneeCollapsePenalty = if (metrics.isFrontCamera) {
            thresholdPenalty(
                value = metrics.maxKneeCollapseRatio,
                soft = LUNGE_KNEE_COLLAPSE_RATIO_THRESHOLD,
                hard = LUNGE_KNEE_COLLAPSE_RATIO_HARD_THRESHOLD
            )
        } else {
            LUNGE_FLOAT_ZERO
        }
        val alignmentPenalty = maxOf(kneeForwardPenalty, kneeCollapsePenalty)
        val posturePenalty = thresholdPenalty(
            value = metrics.maxTorsoLeanAngle,
            soft = LUNGE_TORSO_LEAN_SOFT_THRESHOLD,
            hard = LUNGE_TORSO_LEAN_HARD_THRESHOLD
        )
        val stabilityPenalty = thresholdPenalty(
            value = metrics.stabilityStdDev,
            soft = LUNGE_STABILITY_SWAY_SOFT_THRESHOLD,
            hard = LUNGE_STABILITY_SWAY_HARD_THRESHOLD
        )
        val depthScore = scoreFromPenalty(depthPenalty)
        val alignmentScore = scoreFromPenalty(alignmentPenalty)
        val postureScore = scoreFromPenalty(posturePenalty)
        val stabilityScore = scoreFromPenalty(stabilityPenalty)
        val overallScore = weightedScore(
            depthScore = depthScore,
            alignmentScore = alignmentScore,
            postureScore = postureScore,
            stabilityScore = stabilityScore
        )
        val feedbackKeys = selectFeedbackKeys(
            metrics = metrics,
            frontPenalty = frontDepthPenalty,
            backPenalty = backDepthPenalty,
            kneeForwardPenalty = kneeForwardPenalty,
            torsoPenalty = posturePenalty,
            collapsePenalty = kneeCollapsePenalty,
            stabilityPenalty = stabilityPenalty
        )
        return LungeRepSummary(
            overallScore = overallScore,
            depthScore = depthScore,
            alignmentScore = alignmentScore,
            postureScore = postureScore,
            stabilityScore = stabilityScore,
            feedbackKeys = feedbackKeys,
            frontLeg = metrics.frontLeg,
            frontKneeMinAngle = metrics.frontKneeMinAngle,
            backKneeMinAngle = metrics.backKneeMinAngle,
            maxKneeForwardRatio = metrics.maxKneeForwardRatio,
            maxTorsoLeanAngle = metrics.maxTorsoLeanAngle,
            maxKneeCollapseRatio = metrics.maxKneeCollapseRatio,
            stabilityStdDev = metrics.stabilityStdDev
        )
    }

    private fun weightedScore(
        depthScore: Int,
        alignmentScore: Int,
        postureScore: Int,
        stabilityScore: Int
    ): Int {
        val weighted = depthScore * LUNGE_SCORE_WEIGHT_DEPTH +
                alignmentScore * LUNGE_SCORE_WEIGHT_ALIGNMENT +
                postureScore * LUNGE_SCORE_WEIGHT_POSTURE +
                stabilityScore * LUNGE_SCORE_WEIGHT_STABILITY
        val totalWeight =
            LUNGE_SCORE_WEIGHT_DEPTH +
                    LUNGE_SCORE_WEIGHT_ALIGNMENT +
                    LUNGE_SCORE_WEIGHT_POSTURE +
                    LUNGE_SCORE_WEIGHT_STABILITY
        return (weighted / totalWeight).roundToInt()
    }

    private fun scoreFromPenalty(penalty: Float): Int =
        (LUNGE_SCORE_MAX * (LUNGE_FLOAT_ONE - penalty)).roundToInt()

    private fun rangePenalty(value: Float, min: Float, max: Float, maxDeviation: Float): Float {
        val penalty = when {
            value < min -> (min - value) / maxDeviation
            value > max -> (value - max) / maxDeviation
            else -> LUNGE_FLOAT_ZERO
        }
        return penalty.coerceIn(LUNGE_FLOAT_ZERO, LUNGE_FLOAT_ONE)
    }

    private fun thresholdPenalty(value: Float?, soft: Float, hard: Float): Float {
        if (value == null) return LUNGE_FLOAT_ZERO
        return when {
            value <= soft -> LUNGE_FLOAT_ZERO
            value >= hard -> LUNGE_FLOAT_ONE
            else -> ((value - soft) / (hard - soft)).coerceIn(LUNGE_FLOAT_ZERO, LUNGE_FLOAT_ONE)
        }
    }

    private fun selectFeedbackKeys(
        metrics: LungeRepMetrics,
        frontPenalty: Float,
        backPenalty: Float,
        kneeForwardPenalty: Float,
        torsoPenalty: Float,
        collapsePenalty: Float,
        stabilityPenalty: Float
    ): List<String> {
        val penalties = mutableMapOf<String, Float>()
        if (metrics.frontKneeMinAngle > LUNGE_FRONT_KNEE_TARGET_MAX_ANGLE) {
            penalties[LUNGE_DEPTH_TOO_SHALLOW_FRONT] = frontPenalty
        } else if (metrics.frontKneeMinAngle < LUNGE_FRONT_KNEE_TARGET_MIN_ANGLE) {
            penalties[LUNGE_DEPTH_TOO_DEEP_FRONT] = frontPenalty
        }
        if (metrics.backKneeMinAngle > LUNGE_BACK_KNEE_TARGET_MAX_ANGLE) {
            penalties[LUNGE_DEPTH_TOO_SHALLOW_BACK] = backPenalty
        }
        if (kneeForwardPenalty > LUNGE_FLOAT_ZERO) {
            penalties[LUNGE_KNEE_TOO_FORWARD] = kneeForwardPenalty
        }
        if (torsoPenalty > LUNGE_FLOAT_ZERO) {
            penalties[LUNGE_TORSO_TOO_LEAN_FORWARD] = torsoPenalty
        }
        if (collapsePenalty > LUNGE_FLOAT_ZERO) {
            penalties[LUNGE_KNEE_COLLAPSE_INWARD] = collapsePenalty
        }
        if (stabilityPenalty > LUNGE_FLOAT_ZERO) {
            penalties[LUNGE_UNSTABLE] = stabilityPenalty
        }
        return penalties.entries
            .sortedByDescending { it.value }
            .take(LUNGE_INT_TWO)
            .map { it.key }
    }
}

data class LungeRepMetrics(
    val frontKneeMinAngle: Float,
    val backKneeMinAngle: Float,
    val maxKneeForwardRatio: Float?,
    val maxTorsoLeanAngle: Float,
    val maxKneeCollapseRatio: Float?,
    val stabilityStdDev: Float,
    val isFrontCamera: Boolean,
    val frontLeg: PoseSide
)
