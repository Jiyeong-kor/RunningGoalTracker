package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_HEEL_RISE_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_KNEE_FORWARD_RATIO_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_TRUNK_TO_THIGH_ANGLE_HARD_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_TRUNK_TO_THIGH_ANGLE_SOFT_THRESHOLD
import com.jeong.runninggoaltracker.domain.contract.SQUAT_SHALLOW_DEPTH_ANGLE_THRESHOLD
import com.jeong.runninggoaltracker.domain.model.SquatFormGrade
import com.jeong.runninggoaltracker.domain.model.SquatFormIssue
import com.jeong.runninggoaltracker.domain.model.SquatHeuristicConfig
import com.jeong.runninggoaltracker.domain.model.SquatRepSummary

data class SquatRepMetrics(
    val minKneeAngle: Float,
    val minTrunkToThighAngle: Float,
    val maxHeelRiseRatio: Float?,
    val maxKneeForwardRatio: Float?
)

class SquatFormScorer(
    private val goodDepthAngle: Float = SQUAT_GOOD_DEPTH_ANGLE_THRESHOLD,
    private val shallowDepthAngle: Float = SQUAT_SHALLOW_DEPTH_ANGLE_THRESHOLD,
    private val trunkToThighSoftThreshold: Float = SQUAT_TRUNK_TO_THIGH_ANGLE_SOFT_THRESHOLD,
    private val trunkToThighHardThreshold: Float = SQUAT_TRUNK_TO_THIGH_ANGLE_HARD_THRESHOLD,
    private val heuristicConfig: SquatHeuristicConfig = defaultHeuristicConfig()
) {
    fun score(metrics: SquatRepMetrics): SquatRepSummary {
        val issues = mutableListOf<SquatFormIssue>()
        val depthGrade = when {
            metrics.minKneeAngle > shallowDepthAngle -> {
                issues.add(SquatFormIssue.INSUFFICIENT_DEPTH)
                SquatFormGrade.BAD_FORM
            }

            metrics.minKneeAngle > goodDepthAngle -> {
                issues.add(SquatFormIssue.INSUFFICIENT_DEPTH)
                SquatFormGrade.OK_WITH_WARNINGS
            }

            else -> SquatFormGrade.OK
        }
        when {
            metrics.minTrunkToThighAngle < trunkToThighHardThreshold ->
                issues.add(SquatFormIssue.EXCESS_TRUNK_LEAN_HARD)

            metrics.minTrunkToThighAngle < trunkToThighSoftThreshold ->
                issues.add(SquatFormIssue.EXCESS_TRUNK_LEAN_SOFT)
        }
        if (heuristicConfig.enableHeelRiseProxy) {
            val heelRatio = metrics.maxHeelRiseRatio
            if (heelRatio != null && heelRatio > heuristicConfig.heelRiseRatioThreshold) {
                issues.add(SquatFormIssue.HEEL_RISE)
            }
        }
        if (heuristicConfig.enableKneeForwardProxy) {
            val kneeRatio = metrics.maxKneeForwardRatio
            if (kneeRatio != null && kneeRatio > heuristicConfig.kneeForwardRatioThreshold) {
                issues.add(SquatFormIssue.KNEE_FORWARD_TRANSLATION)
            }
        }
        val grade = when {
            depthGrade == SquatFormGrade.BAD_FORM -> SquatFormGrade.BAD_FORM
            issues.isNotEmpty() -> SquatFormGrade.OK_WITH_WARNINGS
            else -> SquatFormGrade.OK
        }
        return SquatRepSummary(
            minKneeAngle = metrics.minKneeAngle,
            minTrunkToThighAngle = metrics.minTrunkToThighAngle,
            maxHeelRiseRatio = metrics.maxHeelRiseRatio,
            maxKneeForwardRatio = metrics.maxKneeForwardRatio,
            grade = grade,
            issues = issues
        )
    }
}

private fun defaultHeuristicConfig(): SquatHeuristicConfig =
    SquatHeuristicConfig(
        enableHeelRiseProxy = true,
        heelRiseRatioThreshold = SQUAT_HEEL_RISE_RATIO_THRESHOLD,
        enableKneeForwardProxy = true,
        kneeForwardRatioThreshold = SQUAT_KNEE_FORWARD_RATIO_THRESHOLD
    )
