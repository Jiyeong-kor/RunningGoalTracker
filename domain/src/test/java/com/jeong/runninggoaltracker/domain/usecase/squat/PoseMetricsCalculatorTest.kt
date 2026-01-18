package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PoseLandmark
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
import com.jeong.runninggoaltracker.domain.model.PoseSide
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PoseMetricsCalculatorTest {
    @Test
    fun `knee angle and trunk lean are calculated for confident landmarks`() {
        val frame = PoseFrame(
            landmarks = listOf(
                landmark(PoseLandmarkType.LEFT_SHOULDER, 0f, -1f, 1f),
                landmark(PoseLandmarkType.LEFT_HIP, 0f, 0f, 1f),
                landmark(PoseLandmarkType.LEFT_KNEE, 0f, 1f, 1f),
                landmark(PoseLandmarkType.LEFT_ANKLE, 1f, 1f, 1f)
            ),
            timestampMs = 0L,
            imageWidth = 100,
            imageHeight = 100,
            rotationDegrees = 0,
            isFrontCamera = true,
            isMirrored = true
        )
        val calculator = PoseMetricsCalculator()

        val metrics = calculator.calculate(frame, calibration = null, side = PoseSide.LEFT)

        assertEquals(90f, metrics?.kneeAngle ?: 0f, 0.1f)
        assertEquals(0f, metrics?.trunkLeanAngle ?: 0f, 0.1f)
    }

    @Test
    fun `returns null when confidence is below threshold`() {
        val frame = PoseFrame(
            landmarks = listOf(
                landmark(PoseLandmarkType.LEFT_SHOULDER, 0f, -1f, 0.1f),
                landmark(PoseLandmarkType.LEFT_HIP, 0f, 0f, 0.1f),
                landmark(PoseLandmarkType.LEFT_KNEE, 0f, 1f, 0.1f),
                landmark(PoseLandmarkType.LEFT_ANKLE, 1f, 1f, 0.1f)
            ),
            timestampMs = 0L,
            imageWidth = 100,
            imageHeight = 100,
            rotationDegrees = 0,
            isFrontCamera = true,
            isMirrored = true
        )
        val calculator = PoseMetricsCalculator()

        val metrics = calculator.calculate(frame, calibration = null, side = PoseSide.LEFT)

        assertNull(metrics)
    }

    private fun landmark(
        type: PoseLandmarkType,
        x: Float,
        y: Float,
        confidence: Float
    ): PoseLandmark =
        PoseLandmark(type = type, x = x, y = y, z = 0f, confidence = confidence)
}
