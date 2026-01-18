package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_NEGATIVE_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.model.PoseLandmark
import kotlin.math.acos
import kotlin.math.sqrt

class AngleCalculator {
    fun kneeAngle(hip: PoseLandmark, knee: PoseLandmark, ankle: PoseLandmark): Float? =
        angle(hip, knee, ankle)

    fun trunkTiltVerticalAngle(shoulder: PoseLandmark, hip: PoseLandmark): Float? =
        angleBetweenVectors(
            floatArrayOf(shoulder.x - hip.x, shoulder.y - hip.y),
            floatArrayOf(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_NEGATIVE_ONE)
        )

    fun trunkToThighAngle(shoulder: PoseLandmark, hip: PoseLandmark, knee: PoseLandmark): Float? =
        angle(shoulder, hip, knee)

    fun angle(first: PoseLandmark, middle: PoseLandmark, last: PoseLandmark): Float? =
        angleBetweenVectors(
            floatArrayOf(first.x - middle.x, first.y - middle.y),
            floatArrayOf(last.x - middle.x, last.y - middle.y)
        )

    private fun angleBetweenVectors(vectorA: FloatArray, vectorB: FloatArray): Float? {
        val dot =
            vectorA[SQUAT_INT_ZERO] * vectorB[SQUAT_INT_ZERO] + vectorA[SQUAT_INT_ONE] * vectorB[SQUAT_INT_ONE]
        val normA =
            sqrt(vectorA[SQUAT_INT_ZERO] * vectorA[SQUAT_INT_ZERO] + vectorA[SQUAT_INT_ONE] * vectorA[SQUAT_INT_ONE])
        val normB =
            sqrt(vectorB[SQUAT_INT_ZERO] * vectorB[SQUAT_INT_ZERO] + vectorB[SQUAT_INT_ONE] * vectorB[SQUAT_INT_ONE])
        if (normA == SQUAT_FLOAT_ZERO || normB == SQUAT_FLOAT_ZERO) return null
        val cosValue = (dot / (normA * normB)).coerceIn(SQUAT_FLOAT_NEGATIVE_ONE, SQUAT_FLOAT_ONE)
        return Math.toDegrees(acos(cosValue).toDouble()).toFloat()
    }
}
