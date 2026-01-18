package com.jeong.runninggoaltracker.domain.model

enum class ComparisonOperator {
    LESS_THAN,
    GREATER_THAN
}

enum class SquatWarningMetric {
    TRUNK_TO_THIGH,
    KNEE_ANGLE,
    HEEL_RISE_RATIO,
    KNEE_FORWARD_RATIO
}

data class PostureWarningEvent(
    val feedbackType: PostureFeedbackType,
    val metric: SquatWarningMetric,
    val value: Float,
    val threshold: Float,
    val operator: ComparisonOperator,
    val phase: SquatPhase,
    val timestampMs: Long
)
