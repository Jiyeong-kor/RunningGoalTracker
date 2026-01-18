package com.jeong.runninggoaltracker.domain.model

data class SquatRepSummary(
    val minKneeAngle: Float,
    val minTrunkToThighAngle: Float,
    val maxHeelRiseRatio: Float?,
    val maxKneeForwardRatio: Float?,
    val grade: SquatFormGrade,
    val issues: List<SquatFormIssue>
)
