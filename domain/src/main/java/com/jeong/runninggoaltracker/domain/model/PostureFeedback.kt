package com.jeong.runninggoaltracker.domain.model

enum class PostureFeedbackType {
    GOOD_FORM,
    EXCESS_FORWARD_LEAN,
    HEEL_RISE,
    KNEE_FORWARD,
    TOO_SHALLOW,
    STAND_TALL,
    NOT_IN_FRAME,
    UNKNOWN
}

data class PostureFeedback(
    val type: PostureFeedbackType,
    val isValid: Boolean,
    val accuracy: Float,
    val isPerfectForm: Boolean
)
