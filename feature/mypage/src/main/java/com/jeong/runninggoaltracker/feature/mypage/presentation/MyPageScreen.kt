package com.jeong.runninggoaltracker.feature.mypage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingLg
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingMd
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingSm
import com.jeong.runninggoaltracker.feature.mypage.R
import java.util.Locale

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel,
    onNavigateToGoal: () -> Unit,
    onNavigateToReminder: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    MyPageContent(
        uiState = uiState,
        onNavigateToGoal = onNavigateToGoal,
        onNavigateToReminder = onNavigateToReminder,
        onActivityToggle = viewModel::toggleActivityRecognition
    )
}

@Composable
private fun MyPageContent(
    uiState: MyPageUiState,
    onNavigateToGoal: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onActivityToggle: (Boolean) -> Unit
) {
    Scaffold(
        topBar = { }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ProfileSection(uiState.userNickname, uiState.userLevel, uiState.isAnonymous)

            if (uiState.isAnonymous) {
                AppSurfaceCard(contentPadding = PaddingValues(appSpacingLg())) {
                    Column(verticalArrangement = Arrangement.spacedBy(appSpacingMd())) {
                        Text(
                            text = stringResource(id = R.string.mypage_anonymous_info_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(id = R.string.mypage_anonymous_info_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = {}) {
                            Text(text = stringResource(id = R.string.mypage_btn_upgrade_account))
                        }
                    }
                }
            }

            SummaryStats(uiState)

            GoalProgressCard(uiState, onNavigateToGoal)

            SettingsList(
                uiState = uiState,
                onNavigateToReminder = onNavigateToReminder,
                onNavigateToGoal = onNavigateToGoal,
                onActivityToggle = onActivityToggle
            )
        }
    }
}

@Composable
private fun ProfileSection(name: String, level: String, isAnonymous: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(80.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(appSpacingSm())
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (isAnonymous) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = stringResource(id = R.string.mypage_guest_mode_status),
                        modifier = Modifier.padding(
                            horizontal = appSpacingSm(),
                            vertical = appSpacingSm()
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = level,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SummaryStats(uiState: MyPageUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatItem(
            modifier = Modifier.weight(1f),
            label = "총 거리",
            value = "${
                String.format(
                    Locale.getDefault(),
                    "%.1f",
                    uiState.summary?.totalThisWeekKm ?: 0.0
                )
            }km"
        )
        StatItem(
            modifier = Modifier.weight(1f),
            label = "횟수",
            value = "${uiState.summary?.recordCountThisWeek ?: 0}회"
        )
        StatItem(
            modifier = Modifier.weight(1f),
            label = "달성률",
            value = "${
                String.format(
                    Locale.getDefault(),
                    "%.0f",
                    (uiState.summary?.progress ?: 0f) * 100
                )
            }%"
        )
    }
}

@Composable
private fun StatItem(modifier: Modifier, label: String, value: String) {
    AppContentCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GoalProgressCard(uiState: MyPageUiState, onClick: () -> Unit) {
    AppContentCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "주간 목표 달성률", fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onClick) { Text("상세보기") }
            }
            LinearProgressIndicator(
                progress = { uiState.summary?.progress ?: 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
            )
        }
    }
}

@Composable
private fun SettingsList(
    uiState: MyPageUiState,
    onNavigateToReminder: () -> Unit,
    onNavigateToGoal: () -> Unit,
    onActivityToggle: (Boolean) -> Unit
) {
    AppContentCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SettingItem(Icons.Default.Notifications, "알림 설정", "요일 및 시간 관리", onNavigateToReminder)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingItem(Icons.Default.Edit, "러닝 목표 수정", "거리 및 횟수 변경", onNavigateToGoal)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.primary)
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Text("활동 자동 인식", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "자동으로 러닝을 감지합니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = uiState.isActivityRecognitionEnabled,
                    onCheckedChange = onActivityToggle
                )
            }
        }
    }
}

@Composable
private fun SettingItem(icon: ImageVector, title: String, subTitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    subTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyPageScreenPreview() {
    val uiState = MyPageUiState(
        isLoading = false,
        summary = com.jeong.runninggoaltracker.domain.model.RunningSummary(
            weeklyGoalKm = 15.0,
            totalThisWeekKm = 9.5,
            recordCountThisWeek = 3,
            progress = 0.63f
        ),
        userNickname = "러너",
        userLevel = "Active Runner",
        isActivityRecognitionEnabled = true,
        isAnonymous = true
    )

    RunningGoalTrackerTheme {
        MyPageContent(
            uiState = uiState,
            onNavigateToGoal = {},
            onNavigateToReminder = {},
            onActivityToggle = {}
        )
    }
}
