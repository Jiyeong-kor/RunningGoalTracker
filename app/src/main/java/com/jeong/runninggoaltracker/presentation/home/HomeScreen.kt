package com.jeong.runninggoaltracker.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import com.jeong.runninggoaltracker.presentation.record.ActivityRecognitionStateHolder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    repository: RunningRepository,
    onRecordClick: () -> Unit,
    onGoalClick: () -> Unit,
    onReminderClick: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(repository)
    )

    val state by viewModel.uiState.collectAsState()

    // 활동 인식 상태
    val activityState by ActivityRecognitionStateHolder.state.collectAsState()
    val activityLabel = when (activityState.label) {
        "NO_PERMISSION" -> "권한 필요"
        "REQUEST_FAILED", "SECURITY_EXCEPTION" -> "활동 감지 실패"
        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> "알 수 없음"
        else -> activityState.label   // RUNNING, WALKING, STILL, IN_VEHICLE 등
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("러닝 목표 관리")

        // 현재 활동 상태 요약
        Text("현재 활동 상태: $activityLabel (${activityState.confidence}%)")

        // 목표 정보
        if (state.weeklyGoalKm != null) {
            Text("주간 목표: ${"%.1f".format(state.weeklyGoalKm)} km")
        } else {
            Text("주간 목표: 설정되지 않음")
        }

        Text("이번 주 누적 거리: ${"%.1f".format(state.totalThisWeekKm)} km")
        Text("이번 주 러닝 횟수: ${state.recordCountThisWeek} 회")

        // 달성률 ProgressBar
        LinearProgressIndicator(
            progress = { state.progress },
            modifier = Modifier.fillMaxWidth()
        )

        Text("${(state.progress * 100).toInt()} % 달성")

        Button(
            onClick = onRecordClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("러닝 기록 추가 / 보기")
        }

        Button(
            onClick = onGoalClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("주간 목표 설정")
        }

        Button(
            onClick = onReminderClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("러닝 알림 설정")
        }
    }
}
