package com.jeong.runninggoaltracker.feature.ai_coach.presentation

import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.model.ExerciseType
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.SquatFrameMetrics
import com.jeong.runninggoaltracker.domain.model.SquatRepSummary


data class SmartWorkoutUiState(
    val exerciseType: ExerciseType = ExerciseType.SQUAT,
    val repCount: Int = SQUAT_INT_ZERO,
    val feedbackType: PostureFeedbackType = PostureFeedbackType.UNKNOWN,
    val accuracy: Float = SQUAT_FLOAT_ZERO,
    val isPerfectForm: Boolean = false,
    val poseFrame: PoseFrame? = null,
    val frameMetrics: SquatFrameMetrics? = null,
    val repSummary: SquatRepSummary? = null,
    val isDebugOverlayVisible: Boolean = false
)
