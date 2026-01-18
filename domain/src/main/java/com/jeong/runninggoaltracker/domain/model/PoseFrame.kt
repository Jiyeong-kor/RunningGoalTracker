package com.jeong.runninggoaltracker.domain.model

data class PoseFrame(
    val landmarks: List<PoseLandmark>,
    val timestampMs: Long,
    val imageWidth: Int,
    val imageHeight: Int,
    val rotationDegrees: Int,
    val isFrontCamera: Boolean,
    val isMirrored: Boolean
) {
    fun landmark(type: PoseLandmarkType): PoseLandmark? = landmarks.firstOrNull { it.type == type }
}
