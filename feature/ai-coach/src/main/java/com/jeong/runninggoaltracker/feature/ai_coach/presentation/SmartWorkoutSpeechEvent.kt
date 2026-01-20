package com.jeong.runninggoaltracker.feature.ai_coach.presentation

import com.jeong.runninggoaltracker.domain.model.ExerciseType
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType

data class SmartWorkoutSpeechEvent(
    val feedbackType: PostureFeedbackType,
    val feedbackKeys: List<String>,
    val exerciseType: ExerciseType
)
