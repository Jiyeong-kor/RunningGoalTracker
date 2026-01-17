package com.jeong.runninggoaltracker.feature.mypage.presentation

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningSummary


data class MyPageUiState(
    val isLoading: Boolean = true,
    val summary: RunningSummary? = null,
    val goal: RunningGoal? = null,
    val isActivityRecognitionEnabled: Boolean = true,
    val isAnonymous: Boolean = false,
    val userNickname: String? = null,
    val userLevel: String? = null
)
