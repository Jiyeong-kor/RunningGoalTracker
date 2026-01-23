package com.jeong.runninggoaltracker.feature.ai_coach.presentation

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.model.ExerciseType
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.PostureWarningEvent
import com.jeong.runninggoaltracker.domain.model.SquatFrameMetrics
import com.jeong.runninggoaltracker.domain.model.SquatPhaseTransition
import com.jeong.runninggoaltracker.domain.usecase.ProcessPoseUseCase
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutLogContract
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutSpeechContract
import com.jeong.runninggoaltracker.feature.ai_coach.data.pose.PoseDetector
import com.jeong.runninggoaltracker.feature.ai_coach.logging.SmartWorkoutLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class AiCoachViewModel @Inject constructor(
    private val poseDetector: PoseDetector,
    private val processPoseUseCase: ProcessPoseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SmartWorkoutUiState())
    val uiState: StateFlow<SmartWorkoutUiState> = _uiState.asStateFlow()
    private val _speechEvents = MutableSharedFlow<SmartWorkoutSpeechEvent>(extraBufferCapacity = 1)
    val speechEvents = _speechEvents.asSharedFlow()
    private var lastSpokenType: PostureFeedbackType? = null
    private var lastSpokenKey: String? = null
    private var lastSpokenTimestampMs: Long = SmartWorkoutSpeechContract.DEFAULT_COOLDOWN_MS
    private var speechCooldownMs: Long = SmartWorkoutSpeechContract.DEFAULT_COOLDOWN_MS
    private var lastAttemptActive: Boolean? = null
    private var lastDepthReached: Boolean? = null
    private var lastFullBodyVisible: Boolean? = null

    val imageAnalyzer: ImageAnalysis.Analyzer
        get() = poseDetector.imageAnalyzer

    init {
        poseDetector.poseFrames
            .onEach { frame ->
                val analysis = processPoseUseCase.analyze(frame, _uiState.value.exerciseType)
                if (analysis.skippedLowConfidence) {
                    logSkippedFrame(frame.timestampMs)
                }
                if (analysis.repCount.isIncremented) {
                    logRepCountUpdate(
                        source = SmartWorkoutLogContract.SOURCE_ANALYZER,
                        repCount = analysis.repCount.value,
                        timestampMs = frame.timestampMs
                    )
                }
                val feedbackTypeForUi = analysis.feedbackEvent?.let { feedbackType ->
                    if (handleSpeechFeedback(
                            feedbackType = feedbackType,
                            feedbackEventKey = analysis.feedbackEventKey,
                            exerciseType = _uiState.value.exerciseType,
                            timestampMs = frame.timestampMs
                        )
                    ) {
                        feedbackType
                    } else {
                        null
                    }
                }
                val feedbackResId = FeedbackStringMapper.feedbackResId(
                    exerciseType = _uiState.value.exerciseType,
                    feedbackType = feedbackTypeForUi ?: analysis.feedback.type,
                    feedbackKey = analysis.feedbackEventKey
                )
                analysis.frameMetrics?.let { metrics ->
                    metrics.transition?.let { transition ->
                        logTransition(transition, metrics)
                    }
                    logMetricsState(metrics, frame.timestampMs)
                }
                analysis.warningEvent?.let { event ->
                    logWarningEvent(event)
                }
                _uiState.update { current ->
                    current.copy(
                        repCount = analysis.repCount.value,
                        feedbackType = feedbackTypeForUi ?: current.feedbackType,
                        feedbackKeys = listOfNotNull(analysis.feedbackEventKey),
                        feedbackResId = feedbackResId,
                        accuracy = analysis.feedback.accuracy,
                        isPerfectForm = analysis.feedback.isPerfectForm,
                        poseFrame = frame,
                        frameMetrics = analysis.frameMetrics,
                        repSummary = analysis.repSummary,
                        lungeDebugInfo = analysis.lungeDebugInfo,
                        lastLungeRepSnapshot = updateLungeSnapshot(
                            analysis = analysis,
                            exerciseType = _uiState.value.exerciseType,
                            timestampMs = frame.timestampMs,
                            current = current.lastLungeRepSnapshot
                        )
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSpeechCooldown(cooldownMs: Long) = Unit.also { speechCooldownMs = cooldownMs }

    fun toggleDebugOverlay() {
        _uiState.update { current ->
            val nextMode = if (current.overlayMode == DebugOverlayMode.OFF) {
                overlayModeFor(current.exerciseType)
            } else {
                DebugOverlayMode.OFF
            }
            current.copy(overlayMode = nextMode)
        }
    }

    fun updateExerciseType(exerciseType: ExerciseType) {
        _uiState.update { current ->
            if (current.exerciseType == exerciseType) {
                current
            } else {
                current.copy(
                    exerciseType = exerciseType,
                    repCount = SQUAT_INT_ZERO,
                    feedbackType = PostureFeedbackType.UNKNOWN,
                    feedbackKeys = emptyList(),
                    accuracy = SQUAT_FLOAT_ZERO,
                    isPerfectForm = false,
                    overlayMode = if (current.overlayMode == DebugOverlayMode.OFF) {
                        current.overlayMode
                    } else {
                        overlayModeFor(exerciseType)
                    }
                )
            }
        }
        lastSpokenType = null
        lastSpokenKey = null
    }

    override fun onCleared() {
        poseDetector.clear()
        super.onCleared()
    }

    private fun handleSpeechFeedback(
        feedbackType: PostureFeedbackType,
        feedbackEventKey: String?,
        exerciseType: ExerciseType,
        timestampMs: Long
    ): Boolean {
        if (feedbackEventKey == null) {
            return false
        }
        val lastType = lastSpokenType
        val key = feedbackEventKey
        val isChanged = lastType != feedbackType || lastSpokenKey != key
        val elapsedMs = timestampMs - lastSpokenTimestampMs
        val shouldEmit = isChanged || elapsedMs >= speechCooldownMs
        if (shouldEmit) {
            val feedbackResId = FeedbackStringMapper.feedbackResId(
                exerciseType = exerciseType,
                feedbackType = feedbackType,
                feedbackKey = feedbackEventKey
            )
            _speechEvents.tryEmit(
                SmartWorkoutSpeechEvent(
                    feedbackType = feedbackType,
                    feedbackResId = feedbackResId,
                    exerciseType = exerciseType
                )
            )
            lastSpokenType = feedbackType
            lastSpokenKey = key
            lastSpokenTimestampMs = timestampMs
            logFeedbackEvent(feedbackType, key, timestampMs)
        }
        return shouldEmit
    }

    private fun updateLungeSnapshot(
        analysis: com.jeong.runninggoaltracker.domain.model.PoseAnalysisResult,
        exerciseType: ExerciseType,
        timestampMs: Long,
        current: LungeRepSnapshot?
    ): LungeRepSnapshot? {
        if (exerciseType != ExerciseType.LUNGE || !analysis.repCount.isIncremented) {
            return current
        }
        val summary = analysis.lungeRepSummary
        val debugInfo = analysis.lungeDebugInfo
        val feedbackKeys = summary?.feedbackKeys ?: emptyList()
        val goodFormReason = when {
            summary == null -> LungeGoodFormReason.NO_SUMMARY
            feedbackKeys.isNotEmpty() -> LungeGoodFormReason.FEEDBACK_KEYS_PRESENT
            else -> LungeGoodFormReason.GOOD_FORM
        }
        return LungeRepSnapshot(
            timestampMs = timestampMs,
            activeSide = debugInfo?.activeSide,
            countingSide = debugInfo?.countingSide,
            feedbackType = analysis.feedbackEvent,
            feedbackEventKey = analysis.feedbackEventKey,
            feedbackKeys = feedbackKeys,
            overallScore = summary?.overallScore,
            frontKneeMinAngle = summary?.frontKneeMinAngle,
            backKneeMinAngle = summary?.backKneeMinAngle,
            maxTorsoLeanAngle = summary?.maxTorsoLeanAngle,
            stabilityStdDev = summary?.stabilityStdDev,
            maxKneeForwardRatio = summary?.maxKneeForwardRatio,
            maxKneeCollapseRatio = summary?.maxKneeCollapseRatio,
            goodFormReason = goodFormReason
        )
    }

    private fun overlayModeFor(exerciseType: ExerciseType): DebugOverlayMode = when (exerciseType) {
        ExerciseType.LUNGE -> DebugOverlayMode.LUNGE
        else -> DebugOverlayMode.GENERAL
    }

    private fun logTransition(
        transition: SquatPhaseTransition,
        frameMetrics: SquatFrameMetrics
    ): Unit = SmartWorkoutLogger.logDebug {
        buildString {
            append(SmartWorkoutLogContract.TRANSITION_PREFIX)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_FROM)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(transition.from.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TO)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(transition.to.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TIMESTAMP)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(transition.timestampMs)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_REASON)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(transition.reason)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_SIDE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(frameMetrics.side.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_PHASE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(frameMetrics.phase.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_KNEE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(frameMetrics.kneeAngleEma)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TRUNK_TILT)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(frameMetrics.trunkTiltVerticalAngleEma)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TRUNK_TO_THIGH)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(frameMetrics.trunkToThighAngleEma)
        }
    }

    private fun logRepCountUpdate(source: String, repCount: Int, timestampMs: Long): Unit =
        SmartWorkoutLogger.logDebug {
            buildString {
                append(SmartWorkoutLogContract.EVENT_REP_COUNT)
                append(SmartWorkoutLogContract.LOG_SEPARATOR)
                append(SmartWorkoutLogContract.KEY_SOURCE)
                append(SmartWorkoutLogContract.LOG_ASSIGN)
                append(source)
                append(SmartWorkoutLogContract.LOG_SEPARATOR)
                append(SmartWorkoutLogContract.KEY_TIMESTAMP)
                append(SmartWorkoutLogContract.LOG_ASSIGN)
                append(timestampMs)
                append(SmartWorkoutLogContract.LOG_SEPARATOR)
                append(SmartWorkoutLogContract.KEY_REP_COUNT)
                append(SmartWorkoutLogContract.LOG_ASSIGN)
                append(repCount)
            }
        }

    private fun logWarningEvent(event: PostureWarningEvent): Unit = SmartWorkoutLogger.logDebug {
        buildString {
            append(SmartWorkoutLogContract.EVENT_WARNING)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_FEEDBACK)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(event.feedbackType.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_METRIC)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(event.metric.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_VALUE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(event.value)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_THRESHOLD)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(event.threshold)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_OPERATOR)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(event.operator.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_STATE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(event.phase.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TIMESTAMP)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(event.timestampMs)
        }
    }

    private fun logMetricsState(
        metrics: SquatFrameMetrics,
        timestampMs: Long
    ): Unit = Unit.also {
        val attemptActive = metrics.attemptActive
        val previousAttempt = lastAttemptActive
        if (previousAttempt == null) {
            if (attemptActive) {
                logAttemptStart(metrics, timestampMs)
            }
        } else if (previousAttempt != attemptActive) {
            if (attemptActive) {
                logAttemptStart(metrics, timestampMs)
            } else {
                logAttemptEnd(metrics, timestampMs)
            }
        }
        lastAttemptActive = attemptActive
        val depthReached = metrics.depthReached
        val previousDepthReached = lastDepthReached
        if (depthReached && previousDepthReached != true) {
            logDepthReached(metrics, timestampMs)
        }
        lastDepthReached = depthReached
        val fullBodyVisible = metrics.fullBodyVisible
        val previousFullBodyVisible = lastFullBodyVisible
        if (previousFullBodyVisible == null || previousFullBodyVisible != fullBodyVisible) {
            logFullBodyVisibility(metrics, timestampMs)
        }
        lastFullBodyVisible = fullBodyVisible
    }

    private fun logAttemptStart(
        metrics: SquatFrameMetrics,
        timestampMs: Long
    ): Unit = SmartWorkoutLogger.logDebug {
        buildString {
            append(SmartWorkoutLogContract.EVENT_ATTEMPT_START)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TIMESTAMP)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(timestampMs)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_ATTEMPT_ACTIVE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.attemptActive)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_KNEE_MIN)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.attemptMinKneeAngle)
        }
    }

    private fun logAttemptEnd(
        metrics: SquatFrameMetrics,
        timestampMs: Long
    ): Unit = SmartWorkoutLogger.logDebug {
        buildString {
            append(SmartWorkoutLogContract.EVENT_ATTEMPT_END)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TIMESTAMP)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(timestampMs)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_ATTEMPT_ACTIVE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.attemptActive)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_DEPTH_REACHED)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.depthReached)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_KNEE_MIN)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.attemptMinKneeAngle)
        }
    }

    private fun logDepthReached(
        metrics: SquatFrameMetrics,
        timestampMs: Long
    ): Unit = SmartWorkoutLogger.logDebug {
        buildString {
            append(SmartWorkoutLogContract.EVENT_DEPTH_REACHED)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TIMESTAMP)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(timestampMs)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_DEPTH_REACHED)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.depthReached)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_KNEE_MIN)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.attemptMinKneeAngle)
        }
    }

    private fun logFullBodyVisibility(
        metrics: SquatFrameMetrics,
        timestampMs: Long
    ): Unit = SmartWorkoutLogger.logDebug {
        buildString {
            append(SmartWorkoutLogContract.EVENT_FULL_BODY_VISIBILITY)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TIMESTAMP)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(timestampMs)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_FULL_BODY_VISIBLE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.fullBodyVisible)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_INVISIBLE_DURATION)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(metrics.fullBodyInvisibleDurationMs)
        }
    }

    private fun logFeedbackEvent(
        feedbackType: PostureFeedbackType,
        feedbackKey: String,
        timestampMs: Long
    ): Unit = SmartWorkoutLogger.logDebug {
        buildString {
            append(SmartWorkoutLogContract.EVENT_FEEDBACK_EMIT)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_FEEDBACK)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(feedbackType.name)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_VALUE)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(feedbackKey)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TIMESTAMP)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(timestampMs)
        }
    }

    private fun logSkippedFrame(timestampMs: Long): Unit = SmartWorkoutLogger.logDebug {
        buildString {
            append(SmartWorkoutLogContract.EVENT_FRAME_SKIP)
            append(SmartWorkoutLogContract.LOG_SEPARATOR)
            append(SmartWorkoutLogContract.KEY_TIMESTAMP)
            append(SmartWorkoutLogContract.LOG_ASSIGN)
            append(timestampMs)
        }
    }
}
