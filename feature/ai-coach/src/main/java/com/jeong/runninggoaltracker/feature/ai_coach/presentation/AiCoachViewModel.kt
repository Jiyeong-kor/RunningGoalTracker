package com.jeong.runninggoaltracker.feature.ai_coach.presentation

import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.SquatFrameMetrics
import com.jeong.runninggoaltracker.domain.model.SquatPhaseTransition
import com.jeong.runninggoaltracker.domain.usecase.ProcessPoseUseCase
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutLogContract
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutSpeechContract
import com.jeong.runninggoaltracker.feature.ai_coach.data.pose.PoseDetector
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
    private val _speechEvents = MutableSharedFlow<PostureFeedbackType>(extraBufferCapacity = 1)
    val speechEvents = _speechEvents.asSharedFlow()
    private var lastSpokenType: PostureFeedbackType? = null
    private var lastSpokenTimestampMs: Long = SmartWorkoutSpeechContract.DEFAULT_COOLDOWN_MS
    private var speechCooldownMs: Long = SmartWorkoutSpeechContract.DEFAULT_COOLDOWN_MS

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
                    handleSpeechFeedback(
                        feedbackType = analysis.feedback.type,
                        timestampMs = frame.timestampMs
                    )
                }
                analysis.frameMetrics?.let { metrics ->
                    metrics.transition?.let { transition ->
                        logTransition(transition, metrics)
                    }
                }
                _uiState.update { current ->
                    current.copy(
                        repCount = analysis.repCount.value,
                        feedbackType = analysis.feedback.type,
                        accuracy = analysis.feedback.accuracy,
                        isPerfectForm = analysis.feedback.isPerfectForm,
                        poseFrame = frame,
                        frameMetrics = analysis.frameMetrics,
                        repSummary = analysis.repSummary
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun updateSpeechCooldown(cooldownMs: Long) = Unit.also { speechCooldownMs = cooldownMs }

    fun updateDebugOverlay(isVisible: Boolean) =
        _uiState.update { current -> current.copy(isDebugOverlayVisible = isVisible) }

    override fun onCleared() {
        poseDetector.clear()
        super.onCleared()
    }

    private fun handleSpeechFeedback(
        feedbackType: PostureFeedbackType,
        timestampMs: Long
    ) {
        val lastType = lastSpokenType
        val isChanged = lastType != feedbackType
        val elapsedMs = timestampMs - lastSpokenTimestampMs
        if (isChanged || elapsedMs >= speechCooldownMs) {
            _speechEvents.tryEmit(feedbackType)
            lastSpokenType = feedbackType
            lastSpokenTimestampMs = timestampMs
        }
    }

    private fun logTransition(
        transition: SquatPhaseTransition,
        frameMetrics: SquatFrameMetrics
    ): Unit = Unit.also {
        Log.d(
            SmartWorkoutLogContract.LOG_TAG,
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
        )
    }

    private fun logRepCountUpdate(source: String, repCount: Int, timestampMs: Long): Unit =
        Unit.also {
            Log.d(
                SmartWorkoutLogContract.LOG_TAG,
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
            )
        }

    private fun logSkippedFrame(timestampMs: Long): Unit = Unit.also {
        Log.d(
            SmartWorkoutLogContract.LOG_TAG,
            buildString {
                append(SmartWorkoutLogContract.EVENT_FRAME_SKIP)
                append(SmartWorkoutLogContract.LOG_SEPARATOR)
                append(SmartWorkoutLogContract.KEY_TIMESTAMP)
                append(SmartWorkoutLogContract.LOG_ASSIGN)
                append(timestampMs)
            }
        )
    }
}
