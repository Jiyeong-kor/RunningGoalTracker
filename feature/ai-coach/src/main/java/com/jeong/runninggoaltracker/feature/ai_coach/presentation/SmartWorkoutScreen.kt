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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ONE
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_TWO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_MILLIS_PER_SECOND
import com.jeong.runninggoaltracker.domain.model.ExerciseType
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PoseLandmarkType
import com.jeong.runninggoaltracker.domain.model.PoseSide
import com.jeong.runninggoaltracker.domain.model.SquatPhase
import com.jeong.runninggoaltracker.feature.ai_coach.BuildConfig
import com.jeong.runninggoaltracker.feature.ai_coach.R
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutAnimationContract
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutLogContract
import com.jeong.runninggoaltracker.feature.ai_coach.logging.SmartWorkoutLogger
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
import kotlin.math.max
import androidx.camera.core.Preview as CameraPreview
import androidx.compose.ui.tooling.preview.Preview as ComposePreview

@Composable
fun SmartWorkoutRoute(
    onBack: () -> Unit,
    viewModel: AiCoachViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsController = remember { SmartWorkoutTtsController(context) }
    val cooldownMs = integerResource(R.integer.smart_workout_feedback_cooldown_ms).toLong()
    val latestContext by rememberUpdatedState(LocalContext.current)

    LaunchedEffect(cooldownMs) {
        viewModel.updateSpeechCooldown(cooldownMs)
    }

    LaunchedEffect(viewModel, ttsController) {
        viewModel.speechEvents.collect { event ->
            val text = latestContext.getString(event.feedbackResId)
            ttsController.speak(text)
        }
    }

    DisposableEffect(ttsController) {
        onDispose {
            ttsController.shutdown()
        }
    }

    SmartWorkoutScreen(
        uiState = uiState,
        imageAnalyzer = viewModel.imageAnalyzer,
        onBack = onBack,
        onToggleDebugOverlay = viewModel::toggleDebugOverlay,
        onExerciseTypeChange = viewModel::updateExerciseType
    )
}

@Composable
fun SmartWorkoutScreen(
    uiState: SmartWorkoutUiState,
    imageAnalyzer: ImageAnalysis.Analyzer,
    onBack: () -> Unit,
    onToggleDebugOverlay: () -> Unit,
    onExerciseTypeChange: (ExerciseType) -> Unit
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
    val onToggleDebugOverlayClick = rememberThrottleClick(onClick = onToggleDebugOverlay)
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

    LaunchedEffect(uiState.repCount) {
        SmartWorkoutLogger.logDebug {
            buildString {
                append(SmartWorkoutLogContract.EVENT_REP_COUNT)
                append(SmartWorkoutLogContract.LOG_SEPARATOR)
                append(SmartWorkoutLogContract.KEY_SOURCE)
                append(SmartWorkoutLogContract.LOG_ASSIGN)
                append(SmartWorkoutLogContract.SOURCE_UI)
                append(SmartWorkoutLogContract.LOG_SEPARATOR)
                append(SmartWorkoutLogContract.KEY_TIMESTAMP)
                append(SmartWorkoutLogContract.LOG_ASSIGN)
                append(System.currentTimeMillis())
                append(SmartWorkoutLogContract.LOG_SEPARATOR)
                append(SmartWorkoutLogContract.KEY_REP_COUNT)
                append(SmartWorkoutLogContract.LOG_ASSIGN)
                append(uiState.repCount)
            }
        }
    }

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
                modifier = Modifier.weight(SQUAT_FLOAT_ONE),
                contentAlignment = Alignment.Center
            ) {
                ExerciseTypeSelector(
                    exerciseType = uiState.exerciseType,
                    onSelect = onExerciseTypeChange
                )
            }
            Box(modifier = Modifier.width(appSpacing2xl()))
        }

        if (BuildConfig.DEBUG) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(
                        end = appSpacingLg(),
                        top = appSpacingXl()
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(appSpacingSm())
            ) {
                Text(
                    text = stringResource(R.string.smart_workout_debug_toggle),
                    color = textMuted,
                    fontSize = accuracyLabelTextSize.value.sp
                )
                Switch(
                    checked = uiState.overlayMode != DebugOverlayMode.OFF,
                    onCheckedChange = { onToggleDebugOverlayClick() }
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = appSpacingXl())
        ) {
            Text(
                text = stringResource(R.string.smart_workout_rep_count_value, uiState.repCount),
                color = textPrimary,
                fontSize = repCountTextSize.value.sp,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Black
            )
        }

        if (BuildConfig.DEBUG && uiState.overlayMode == DebugOverlayMode.LUNGE) {
            LungeDebugOverlay(
                debugInfo = uiState.lungeDebugInfo,
                snapshot = uiState.lastLungeRepSnapshot,
                frameMetrics = uiState.frameMetrics,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        horizontal = appSpacingLg(),
                        vertical = appSpacingLg()
                    )
            )
        }

        if (BuildConfig.DEBUG && uiState.overlayMode == DebugOverlayMode.GENERAL) {
            uiState.frameMetrics?.let { metrics ->
                val phaseText = when (metrics.phase) {
                    SquatPhase.UP -> stringResource(R.string.smart_workout_phase_up)
                    SquatPhase.DOWN -> stringResource(R.string.smart_workout_phase_down)
                }
                val sideText = when (metrics.side) {
                    PoseSide.LEFT -> stringResource(R.string.smart_workout_side_left)
                    PoseSide.RIGHT -> stringResource(R.string.smart_workout_side_right)
                }
                val lockText = if (metrics.isSideLocked) {
                    stringResource(R.string.smart_workout_debug_lock_true)
                } else {
                    stringResource(R.string.smart_workout_debug_lock_false)
                }
                val reliableText = if (metrics.isLandmarkReliable) {
                    stringResource(R.string.smart_workout_reliable_true)
                } else {
                    stringResource(R.string.smart_workout_reliable_false)
                }
                val attemptActiveText = if (metrics.attemptActive) {
                    stringResource(R.string.smart_workout_debug_on)
                } else {
                    stringResource(R.string.smart_workout_debug_off)
                }
                val depthReachedText = if (metrics.depthReached) {
                    stringResource(R.string.smart_workout_debug_on)
                } else {
                    stringResource(R.string.smart_workout_debug_off)
                }
                val fullBodyVisibleText = if (metrics.fullBodyVisible) {
                    stringResource(R.string.smart_workout_debug_on)
                } else {
                    stringResource(R.string.smart_workout_debug_off)
                }
                val invisibleDurationSec =
                    metrics.fullBodyInvisibleDurationMs.toFloat() / SQUAT_MILLIS_PER_SECOND
                val frontCameraText = if (metrics.isFrontCamera) {
                    stringResource(R.string.smart_workout_debug_on)
                } else {
                    stringResource(R.string.smart_workout_debug_off)
                }
                val mirroringText = if (metrics.isMirroringApplied) {
                    stringResource(R.string.smart_workout_debug_on)
                } else {
                    stringResource(R.string.smart_workout_debug_off)
                }
                val cameraTiltText = if (metrics.isCameraTiltSuspected) {
                    stringResource(R.string.smart_workout_debug_on)
                } else {
                    stringResource(R.string.smart_workout_debug_off)
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(
                            horizontal = appSpacingLg(),
                            vertical = appSpacingXl()
                        ),
                    verticalArrangement = Arrangement.spacedBy(appSpacingSm())
                ) {
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_knee_angle_raw,
                            metrics.kneeAngleRaw
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_knee_angle_ema,
                            metrics.kneeAngleEma
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_trunk_tilt_raw,
                            metrics.trunkTiltVerticalAngleRaw
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_trunk_tilt_ema,
                            metrics.trunkTiltVerticalAngleEma
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_trunk_to_thigh_raw,
                            metrics.trunkToThighAngleRaw
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_trunk_to_thigh_ema,
                            metrics.trunkToThighAngleEma
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_rep_min_knee,
                            metrics.repMinKneeAngle
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_rep_trunk_to_thigh_min,
                            metrics.repMinTrunkToThighAngle
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_rep_trunk_tilt_max,
                            metrics.repMaxTrunkTiltVerticalAngle
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_phase,
                            phaseText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_side,
                            sideText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lock_state,
                            lockText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_up_threshold,
                            metrics.upThreshold
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_down_threshold,
                            metrics.downThreshold
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_up_frames,
                            metrics.upFramesRequired
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_down_frames,
                            metrics.downFramesRequired
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_up_frames_count,
                            metrics.upCandidateFrames
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_down_frames_count,
                            metrics.downCandidateFrames
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_left_confidence,
                            metrics.leftConfidenceSum
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_right_confidence,
                            metrics.rightConfidenceSum
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_attempt_active,
                            attemptActiveText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_depth_reached,
                            depthReachedText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_attempt_knee_min,
                            metrics.attemptMinKneeAngle
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_full_body_visible,
                            fullBodyVisibleText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_invisible_duration,
                            invisibleDurationSec
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_rotation,
                            metrics.rotationDegrees
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_front_camera,
                            frontCameraText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_mirroring,
                            mirroringText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_camera_tilt,
                            cameraTiltText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_reliable,
                            reliableText
                        ),
                        color = textMuted,
                        fontSize = accuracyLabelTextSize.value.sp
                    )
                }
            }
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
                    text = stringResource(uiState.feedbackResId),
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
private fun ExerciseTypeSelector(
    exerciseType: ExerciseType,
    onSelect: (ExerciseType) -> Unit,
    modifier: Modifier = Modifier
) {
    val onSelectSquat = rememberThrottleClick {
        onSelect(ExerciseType.SQUAT)
    }
    val onSelectLunge = rememberThrottleClick {
        onSelect(ExerciseType.LUNGE)
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(appSpacingSm())
    ) {
        ExerciseTypeOption(
            label = exerciseTypeLabel(ExerciseType.SQUAT),
            isSelected = exerciseType == ExerciseType.SQUAT,
            onClick = onSelectSquat
        )
        ExerciseTypeOption(
            label = exerciseTypeLabel(ExerciseType.LUNGE),
            isSelected = exerciseType == ExerciseType.LUNGE,
            onClick = onSelectLunge
        )
    }
}

@Composable
private fun ExerciseTypeOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        appTextPrimaryColor()
    }
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = appSpacingMd(), vertical = appSpacingSm()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
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
            CameraSelector.DEFAULT_FRONT_CAMERA,
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
        val imageWidth = frame.imageWidth.toFloat()
        val imageHeight = frame.imageHeight.toFloat()
        val hasValidSize = imageWidth > SQUAT_FLOAT_ZERO && imageHeight > SQUAT_FLOAT_ZERO
        val scale = if (hasValidSize) {
            max(size.width / imageWidth, size.height / imageHeight)
        } else {
            SQUAT_FLOAT_ONE
        }
        val scaledWidth = imageWidth * scale
        val scaledHeight = imageHeight * scale
        val offsetX = (size.width - scaledWidth) / SQUAT_FLOAT_TWO
        val offsetY = (size.height - scaledHeight) / SQUAT_FLOAT_TWO

        fun toOffset(landmarkX: Float, landmarkY: Float): Offset = Offset(
            x = landmarkX * scaledWidth + offsetX,
            y = landmarkY * scaledHeight + offsetY
        )

        connections.forEach { (startType, endType) ->
            val start = points[startType]
            val end = points[endType]
            if (start != null && end != null) {
                drawLine(
                    color = strokeColor,
                    start = toOffset(start.x, start.y),
                    end = toOffset(end.x, end.y),
                    strokeWidth = skeletonStrokeWidthPx,
                    cap = StrokeCap.Round
                )
            }
        }

        points.values.forEach { landmark ->
            drawCircle(
                color = strokeColor,
                radius = skeletonDotRadiusPx,
                center = toOffset(landmark.x, landmark.y)
            )
        }
    }
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
            onBack = {},
            onToggleDebugOverlay = {},
            onExerciseTypeChange = {},
        )
    }
}
