package com.jeong.runninggoaltracker.domain.model

data class SquatFrameMetrics(
    val kneeAngle: Float,
    val trunkLeanAngle: Float,
    val heelRiseRatio: Float?,
    val kneeForwardRatio: Float?,
    val phase: SquatPhase,
    val side: PoseSide,
    val upThreshold: Float,
    val downThreshold: Float,
    val upFramesRequired: Int,
    val downFramesRequired: Int,
    val transition: SquatPhaseTransition?,
    val isLandmarkReliable: Boolean,
    val isCalibrated: Boolean
)
