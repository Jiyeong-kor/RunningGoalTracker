package com.jeong.runninggoaltracker.domain.model

data class PoseAnalysisResult(
    val repCount: RepCount,
    val feedback: PostureFeedback,
    val frameMetrics: SquatFrameMetrics?,
    val repSummary: SquatRepSummary?,
    val warningEvent: PostureWarningEvent?,
    val skippedLowConfidence: Boolean
)
