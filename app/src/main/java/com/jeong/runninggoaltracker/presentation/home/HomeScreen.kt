package com.jeong.runninggoaltracker.presentation.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import com.jeong.runninggoaltracker.presentation.record.ActivityLogHolder
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
    val activityState by ActivityRecognitionStateHolder.state.collectAsState()
    val activityLogs by ActivityLogHolder.logs.collectAsState()

    val rawLabel = activityState.label
    val activityLabel = when (rawLabel) {
        "NO_PERMISSION" -> "권한 필요"
        "REQUEST_FAILED", "SECURITY_EXCEPTION" -> "활동 감지 실패"
        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> "알 수 없음"
        else -> rawLabel
    }

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    // 활동 상태 칩 색상 애니메이션
    val activityChipColor by animateColorAsState(
        targetValue = when (rawLabel) {
            "RUNNING" -> colorScheme.tertiaryContainer
            "WALKING" -> colorScheme.primaryContainer
            "STILL" -> colorScheme.surfaceVariant
            else -> colorScheme.surfaceContainerLow
        },
        label = "activityChipColor"
    )

    val activityIcon = when (rawLabel) {
        "RUNNING" -> Icons.AutoMirrored.Filled.DirectionsRun
        "WALKING" -> Icons.AutoMirrored.Filled.DirectionsWalk
        "STILL" -> Icons.Filled.SelfImprovement
        else -> Icons.AutoMirrored.Filled.HelpOutline
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 화면 타이틀
        Text(
            text = "러닝 목표 관리",
            style = typography.titleLarge,
            color = colorScheme.onBackground
        )

        // 카드 1: 오늘 상태 / 주간 목표 요약
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "오늘 상태",
                    style = typography.titleMedium,
                    color = colorScheme.onSurface
                )

                // 활동 상태 칩
                Row(
                    modifier = Modifier
                        .wrapContentWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .background(activityChipColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = activityIcon,
                        contentDescription = "현재 활동",
                        tint = colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "$activityLabel  (${activityState.confidence}%)",
                        style = typography.bodyMedium,
                        color = colorScheme.onPrimaryContainer
                    )
                }

                if (state.weeklyGoalKm != null) {
                    Text(
                        text = "주간 목표: ${"%.1f".format(state.weeklyGoalKm)} km",
                        style = typography.bodyLarge
                    )
                } else {
                    Text(
                        text = "주간 목표: 설정되지 않음",
                        style = typography.bodyLarge
                    )
                }

                Text(
                    text = "이번 주 누적 거리: ${"%.1f".format(state.totalThisWeekKm)} km",
                    style = typography.bodyMedium
                )
                Text(
                    text = "이번 주 러닝 횟수: ${state.recordCountThisWeek} 회",
                    style = typography.bodyMedium
                )

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(state.progress * 100).toInt()}% 달성",
                    style = typography.labelSmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // 카드 2: 빠른 메뉴
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "빠른 메뉴",
                    style = typography.titleMedium
                )

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

        // 카드 3: 최근 활동 로그
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "최근 활동 로그",
                    style = typography.titleMedium
                )

                if (activityLogs.isNotEmpty()) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(activityLogs) { log ->
                            Text(
                                text = "${log.time}  •  ${log.label} (${log.confidence}%)",
                                style = typography.bodyMedium
                            )
                        }
                    }
                } else {
                    Text(
                        text = "최근 활동이 없습니다.",
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
