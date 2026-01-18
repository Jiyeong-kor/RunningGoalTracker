package com.jeong.runninggoaltracker.domain.model

data class SquatFrameMetrics(
    val kneeAngleRaw: Float,
    val kneeAngleEma: Float,
    val trunkLeanAngleRaw: Float,
    val trunkLeanAngleEma: Float,
    val heelRiseRatio: Float?,
    val kneeForwardRatio: Float?,
    val phase: SquatPhase,
    val side: PoseSide,
    val isSideLocked: Boolean,
    val upThreshold: Float,
    val downThreshold: Float,
    val upFramesRequired: Int,
    val downFramesRequired: Int,
    val upCandidateFrames: Int,
    val downCandidateFrames: Int,
    val leftConfidenceSum: Float,
    val rightConfidenceSum: Float,
    val rotationDegrees: Int,
    val isFrontCamera: Boolean,
    val isMirroringApplied: Boolean,
    val transition: SquatPhaseTransition?,
    val isLandmarkReliable: Boolean,
    val isCalibrated: Boolean
)
