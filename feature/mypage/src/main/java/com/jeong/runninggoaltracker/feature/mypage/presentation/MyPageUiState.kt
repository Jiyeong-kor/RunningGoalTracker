package com.jeong.runninggoaltracker.feature.mypage.presentation

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningSummary


data class MyPageUiState(
    val isLoading: Boolean = true,
    val summary: RunningSummary? = null,
    val goal: RunningGoal? = null,
    val isActivityRecognitionEnabled: Boolean = true,
    val userNickname: String = "닉네임",
    val userLevel: String = "Active Professional"
)