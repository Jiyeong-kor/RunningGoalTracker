package com.jeong.runninggoaltracker.domain.model

data class SquatPhaseTransition(
    val from: SquatPhase,
    val to: SquatPhase,
    val timestampMs: Long,
    val reason: String
)
