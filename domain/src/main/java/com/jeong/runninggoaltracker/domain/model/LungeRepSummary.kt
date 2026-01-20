package com.jeong.runninggoaltracker.domain.model

data class LungeRepSummary(
    val overallScore: Int,
    val depthScore: Int,
    val alignmentScore: Int,
    val postureScore: Int,
    val stabilityScore: Int,
    val feedbackKeys: List<String>,
    val frontLeg: PoseSide,
    val frontKneeMinAngle: Float,
    val backKneeMinAngle: Float,
    val maxKneeForwardRatio: Float?,
    val maxTorsoLeanAngle: Float,
    val maxKneeCollapseRatio: Float?,
    val stabilityStdDev: Float
)
