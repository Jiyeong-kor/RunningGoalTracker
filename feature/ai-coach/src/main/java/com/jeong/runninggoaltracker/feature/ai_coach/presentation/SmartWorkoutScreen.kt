package com.jeong.runninggoaltracker.feature.ai_coach.presentation

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.domain.model.ExerciseType
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.feature.ai_coach.R
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutAnimationContract
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import com.jeong.runninggoaltracker.shared.designsystem.theme.appAccentColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacing2xl
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingLg
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingMd
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingSm
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingXl
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSurfaceColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextMutedColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextPrimaryColor
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import androidx.camera.core.Preview as CameraPreview
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun SmartWorkoutRoute(
    onBack: () -> Unit,
    viewModel: AiCoachViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SmartWorkoutScreen(
        uiState = uiState,
        imageAnalyzer = viewModel.imageAnalyzer,
        onBack = onBack
    )
}

@Composable
fun SmartWorkoutScreen(
    uiState: SmartWorkoutUiState,
    imageAnalyzer: ImageAnalysis.Analyzer,
    onBack: () -> Unit
) {
    val accentColor = appAccentColor()
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()
    val repCountTextSize = dimensionResource(R.dimen.smart_workout_rep_count_text_size)
    val feedbackTitleTextSize = dimensionResource(R.dimen.smart_workout_feedback_title_text_size)
    val feedbackBodyTextSize = dimensionResource(R.dimen.smart_workout_feedback_body_text_size)
    val accuracyLabelTextSize = dimensionResource(R.dimen.smart_workout_accuracy_label_text_size)
    val accuracyMultiplier = integerResource(R.integer.smart_workout_accuracy_percent_multiplier)
    val onBackClick = rememberThrottleClick(onClick = onBack)
    val containerColor by animateColorAsState(
        targetValue = if (uiState.isPerfectForm) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            appSurfaceColor()
        },
        label = SmartWorkoutAnimationContract.FEEDBACK_CARD_ANIMATION_LABEL
    )
    val accuracyProgress by animateFloatAsState(
        targetValue = uiState.accuracy,
        label = SmartWorkoutAnimationContract.ACCURACY_PROGRESS_ANIMATION_LABEL
    )

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            imageAnalyzer = imageAnalyzer,
            modifier = Modifier.fillMaxSize()
        )

        SkeletonOverlay(
            poseFrame = uiState.poseFrame,
            strokeColor = accentColor,
            modifier = Modifier.fillMaxSize()
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(
                    horizontal = appSpacingLg(),
                    vertical = appSpacingXl()
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.smart_workout_close),
                    tint = textPrimary
                )
            }
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                ExerciseTypeChip(exerciseType = uiState.exerciseType)
            }
            Box(modifier = Modifier.width(appSpacing2xl()))
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = appSpacingXl())
        ) {
            Text(
                text = uiState.repCount.toString(),
                color = textPrimary,
                fontSize = repCountTextSize.value.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Black
            )
        }

        AppSurfaceCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    horizontal = appSpacingLg(),
                    vertical = appSpacingXl()
                ),
            containerColor = containerColor,
            contentPadding = PaddingValues(appSpacingLg())
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(appSpacingSm())) {
                Text(
                    text = stringResource(R.string.smart_workout_feedback_title),
                    color = textMuted,
                    fontSize = feedbackTitleTextSize.value.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = feedbackText(uiState.feedbackType),
                    color = textPrimary,
                    fontSize = feedbackBodyTextSize.value.sp,
                    fontWeight = FontWeight.SemiBold
                )
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { accuracyProgress },
                    color = accentColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = stringResource(
                        R.string.smart_workout_accuracy_label,
                        (accuracyProgress * accuracyMultiplier).toInt()
                    ),
                    color = textMuted,
                    fontSize = accuracyLabelTextSize.value.sp
                )
            }
        }
    }
}

@Composable
private fun ExerciseTypeChip(
    exerciseType: ExerciseType,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = appSpacingMd(), vertical = appSpacingSm()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = exerciseTypeLabel(exerciseType),
                color = appTextPrimaryColor(),
                fontSize = dimensionResource(R.dimen.smart_workout_chip_text_size).value.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CameraPreview(
    imageAnalyzer: ImageAnalysis.Analyzer,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }

    LaunchedEffect(imageAnalyzer) {
        val cameraProvider = context.awaitCameraProvider()
        val preview = CameraPreview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }
        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(executor, imageAnalyzer)
            }

        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analysis
        )
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}

@Composable
private fun SkeletonOverlay(
    poseFrame: PoseFrame?,
    strokeColor: Color,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val skeletonStrokeWidthPx = with(density) {
        dimensionResource(R.dimen.smart_workout_skeleton_stroke_width).toPx()
    }
    val skeletonDotRadiusPx = with(density) {
        dimensionResource(R.dimen.smart_workout_skeleton_dot_radius).toPx()
    }
    val connections = remember {
        listOf(
            PoseLandmarkType.LEFT_SHOULDER to PoseLandmarkType.RIGHT_SHOULDER,
            PoseLandmarkType.LEFT_SHOULDER to PoseLandmarkType.LEFT_HIP,
            PoseLandmarkType.RIGHT_SHOULDER to PoseLandmarkType.RIGHT_HIP,
            PoseLandmarkType.LEFT_HIP to PoseLandmarkType.RIGHT_HIP,
            PoseLandmarkType.LEFT_HIP to PoseLandmarkType.LEFT_KNEE,
            PoseLandmarkType.LEFT_KNEE to PoseLandmarkType.LEFT_ANKLE,
            PoseLandmarkType.RIGHT_HIP to PoseLandmarkType.RIGHT_KNEE,
            PoseLandmarkType.RIGHT_KNEE to PoseLandmarkType.RIGHT_ANKLE
        )
    }

    Canvas(modifier = modifier) {
        val frame = poseFrame ?: return@Canvas
        val points = frame.landmarks.associateBy { it.type }

        connections.forEach { (startType, endType) ->
            val start = points[startType]
            val end = points[endType]
            if (start != null && end != null) {
                drawLine(
                    color = strokeColor,
                    start = Offset(start.x * size.width, start.y * size.height),
                    end = Offset(end.x * size.width, end.y * size.height),
                    strokeWidth = skeletonStrokeWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }

        points.values.forEach { landmark ->
            drawCircle(
                color = strokeColor,
                radius = skeletonDotRadiusPx,
                center = Offset(landmark.x * size.width, landmark.y * size.height)
            )
        }
    }
}

@Composable
private fun feedbackText(type: PostureFeedbackType): String = when (type) {
    PostureFeedbackType.GOOD_FORM -> stringResource(R.string.smart_workout_feedback_good)
    PostureFeedbackType.TOO_SHALLOW -> stringResource(R.string.smart_workout_feedback_shallow)
    PostureFeedbackType.STAND_TALL -> stringResource(R.string.smart_workout_feedback_stand_tall)
    PostureFeedbackType.UNKNOWN -> stringResource(R.string.smart_workout_feedback_unknown)
}

@Composable
private fun exerciseTypeLabel(type: ExerciseType): String = when (type) {
    ExerciseType.SQUAT -> stringResource(R.string.smart_workout_exercise_squat)
    ExerciseType.LUNGE -> stringResource(R.string.smart_workout_exercise_lunge)
    ExerciseType.PUSH_UP -> stringResource(R.string.smart_workout_exercise_push_up)
}

private suspend fun Context.awaitCameraProvider(): ProcessCameraProvider =
    suspendCancellableCoroutine { continuation ->
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener(
            {
                continuation.resume(future.get())
            },
            ContextCompat.getMainExecutor(this)
        )
    }

@ComposePreview(showBackground = true)
@Composable
private fun SmartWorkoutScreenPreview() {
    RunningGoalTrackerTheme {
        SmartWorkoutScreen(
            uiState = SmartWorkoutUiState(),
            imageAnalyzer = { image -> image.close() },
            onBack = {}
        )
    }
}
