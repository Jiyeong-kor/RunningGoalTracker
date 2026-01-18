package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_MIN_LANDMARK_CONFIDENCE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_SIDE_SELECTION_STABLE_WINDOW_MS
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PoseLandmark
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
import com.jeong.runninggoaltracker.domain.model.PoseSide
import com.jeong.runninggoaltracker.domain.model.SquatPhase

data class SideSelectionResult(
    val selectedSide: PoseSide?,
    val isLocked: Boolean,
    val leftConfidenceSum: Float,
    val rightConfidenceSum: Float
)

class SideSelector(
    private val minConfidence: Float = SQUAT_MIN_LANDMARK_CONFIDENCE,
    private val stableWindowMs: Long = SQUAT_SIDE_SELECTION_STABLE_WINDOW_MS
) {
    private var selectedSide: PoseSide? = null
    private var lockedSide: PoseSide? = null
    private var pendingSide: PoseSide? = null
    private var pendingStartMs: Long? = null
    private var upPhaseStartMs: Long? = null

    fun update(frame: PoseFrame, phase: SquatPhase): SideSelectionResult {
        val leftSum = confidenceSum(frame, PoseSide.LEFT)
        val rightSum = confidenceSum(frame, PoseSide.RIGHT)
        val candidateSide = chooseCandidate(leftSum, rightSum)
        val timestampMs = frame.timestampMs
        val isUpPhase = phase == SquatPhase.UP
        if (lockedSide != null && !isUpPhase) {
            return SideSelectionResult(
                selectedSide = lockedSide,
                isLocked = true,
                leftConfidenceSum = leftSum,
                rightConfidenceSum = rightSum
            )
        }
        if (lockedSide != null) {
            val upStart = upPhaseStartMs ?: timestampMs
            upPhaseStartMs = upStart
            if (timestampMs - upStart >= stableWindowMs) {
                lockedSide = null
                pendingSide = null
                pendingStartMs = null
                upPhaseStartMs = null
            }
        }
        if (lockedSide == null) {
            if (candidateSide == null) {
                pendingSide = null
                pendingStartMs = null
            } else if (candidateSide != selectedSide) {
                if (pendingSide != candidateSide) {
                    pendingSide = candidateSide
                    pendingStartMs = timestampMs
                }
                val pendingStart = pendingStartMs ?: timestampMs
                if (timestampMs - pendingStart >= stableWindowMs) {
                    selectedSide = candidateSide
                    pendingSide = null
                    pendingStartMs = null
                }
            }
        } else {
            selectedSide = lockedSide
        }
        if (!isUpPhase && selectedSide != null) {
            lockedSide = selectedSide
        }
        return SideSelectionResult(
            selectedSide = selectedSide,
            isLocked = lockedSide != null,
            leftConfidenceSum = leftSum,
            rightConfidenceSum = rightSum
        )
    }

    private fun chooseCandidate(leftSum: Float, rightSum: Float): PoseSide? = when {
        leftSum == SQUAT_FLOAT_ZERO && rightSum == SQUAT_FLOAT_ZERO -> null
        leftSum >= rightSum -> PoseSide.LEFT
        else -> PoseSide.RIGHT
    }

    private fun confidenceSum(frame: PoseFrame, side: PoseSide): Float {
        val landmarks = landmarksForSide(frame, side)
        if (landmarks.any { it == null }) return SQUAT_FLOAT_ZERO
        val typedLandmarks = landmarks.filterNotNull()
        val minConfidenceValue = typedLandmarks.minOf { it.confidence }
        if (minConfidenceValue < minConfidence) return SQUAT_FLOAT_ZERO
        return typedLandmarks.fold(SQUAT_FLOAT_ZERO) { total, landmark -> total + landmark.confidence }
    }

    private fun landmarksForSide(frame: PoseFrame, side: PoseSide): List<PoseLandmark?> =
        if (side == PoseSide.LEFT) {
            listOf(
                frame.landmark(PoseLandmarkType.LEFT_SHOULDER),
                frame.landmark(PoseLandmarkType.LEFT_HIP),
                frame.landmark(PoseLandmarkType.LEFT_KNEE),
                frame.landmark(PoseLandmarkType.LEFT_ANKLE)
            )
        } else {
            listOf(
                frame.landmark(PoseLandmarkType.RIGHT_SHOULDER),
                frame.landmark(PoseLandmarkType.RIGHT_HIP),
                frame.landmark(PoseLandmarkType.RIGHT_KNEE),
                frame.landmark(PoseLandmarkType.RIGHT_ANKLE)
            )
        }
}
