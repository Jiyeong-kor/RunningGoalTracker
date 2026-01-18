package com.jeong.runninggoaltracker.feature.ai_coach.data.pose

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetector as MlKitPoseDetector
import com.google.mlkit.vision.pose.PoseLandmark as MlKitPoseLandmark
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PoseLandmark
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutPoseContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import java.util.concurrent.TimeUnit

class MlKitPoseDetector(
    private val poseDetector: MlKitPoseDetector,
    private val isFrontCamera: Boolean
) : PoseDetector, ImageAnalysis.Analyzer {
    private val frameFlow = MutableSharedFlow<PoseFrame>(extraBufferCapacity = 1)

    override val poseFrames: Flow<PoseFrame> = frameFlow

    override val imageAnalyzer: ImageAnalysis.Analyzer
        get() = this

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        val rotationDegrees = image.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotationDegrees)
        val timestampMs = TimeUnit.NANOSECONDS.toMillis(image.imageInfo.timestamp)
        poseDetector.process(inputImage)
            .addOnSuccessListener { pose ->
                frameFlow.tryEmit(
                    pose.toPoseFrame(
                        imageWidth = image.width,
                        imageHeight = image.height,
                        rotationDegrees = rotationDegrees,
                        isFrontCamera = isFrontCamera,
                        timestampMs = timestampMs
                    )
                )
            }
            .addOnCompleteListener {
                image.close()
            }
    }

    override fun clear() =
        poseDetector.close()
}

private fun Pose.toPoseFrame(
    imageWidth: Int,
    imageHeight: Int,
    rotationDegrees: Int,
    isFrontCamera: Boolean,
    timestampMs: Long
): PoseFrame {
    val isRightAngle = rotationDegrees % SmartWorkoutPoseContract.ROTATION_HALF_TURN_DEGREES !=
            SmartWorkoutPoseContract.ROTATION_ZERO_DEGREES
    val rotatedWidth = if (isRightAngle) imageHeight else imageWidth
    val rotatedHeight = if (isRightAngle) imageWidth else imageHeight
    val width = rotatedWidth.takeIf { it > SQUAT_INT_ZERO } ?: SQUAT_INT_ONE
    val height = rotatedHeight.takeIf { it > SQUAT_INT_ZERO } ?: SQUAT_INT_ONE
    val landmarks = allPoseLandmarks.mapNotNull { landmark ->
        val type = landmark.landmarkType.toDomainType() ?: return@mapNotNull null
        val normalizedX = (landmark.position.x / width).coerceIn(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_ONE)
        val normalizedY = (landmark.position.y / height).coerceIn(SQUAT_FLOAT_ZERO, SQUAT_FLOAT_ONE)
        val mappedX = if (isFrontCamera) SQUAT_FLOAT_ONE - normalizedX else normalizedX
        PoseLandmark(
            type = type,
            x = mappedX,
            y = normalizedY,
            z = landmark.position3D.z,
            confidence = landmark.inFrameLikelihood
        )
    }
    return PoseFrame(
        landmarks = landmarks,
        timestampMs = timestampMs,
        imageWidth = width,
        imageHeight = height,
        rotationDegrees = rotationDegrees,
        isFrontCamera = isFrontCamera,
        isMirrored = isFrontCamera
    )
}

private fun Int.toDomainType(): PoseLandmarkType? = when (this) {
    MlKitPoseLandmark.NOSE -> PoseLandmarkType.NOSE
    MlKitPoseLandmark.LEFT_SHOULDER -> PoseLandmarkType.LEFT_SHOULDER
    MlKitPoseLandmark.RIGHT_SHOULDER -> PoseLandmarkType.RIGHT_SHOULDER
    MlKitPoseLandmark.LEFT_HIP -> PoseLandmarkType.LEFT_HIP
    MlKitPoseLandmark.RIGHT_HIP -> PoseLandmarkType.RIGHT_HIP
    MlKitPoseLandmark.LEFT_KNEE -> PoseLandmarkType.LEFT_KNEE
    MlKitPoseLandmark.RIGHT_KNEE -> PoseLandmarkType.RIGHT_KNEE
    MlKitPoseLandmark.LEFT_ANKLE -> PoseLandmarkType.LEFT_ANKLE
    MlKitPoseLandmark.RIGHT_ANKLE -> PoseLandmarkType.RIGHT_ANKLE
    else -> null
}
