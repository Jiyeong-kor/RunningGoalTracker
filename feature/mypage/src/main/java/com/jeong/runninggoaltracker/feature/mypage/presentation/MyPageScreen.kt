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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jeong.runninggoaltracker.domain.model.AuthError
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.extension.throttleClick
import com.jeong.runninggoaltracker.shared.designsystem.formatter.DistanceFormatter
import com.jeong.runninggoaltracker.shared.designsystem.formatter.PercentageFormatter
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingLg
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingMd
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingSm
import com.jeong.runninggoaltracker.feature.mypage.R

@Composable
fun MyPageScreen(
    viewModel: MyPageViewModel,
    onNavigateToGoal: () -> Unit,
    onNavigateToReminder: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteAccountState by viewModel.deleteAccountState.collectAsState()

    MyPageContent(
        uiState = uiState,
        onNavigateToGoal = onNavigateToGoal,
        onNavigateToReminder = onNavigateToReminder,
        onActivityToggle = viewModel::toggleActivityRecognition,
        onDeleteAccount = viewModel::deleteAccount,
        deleteAccountState = deleteAccountState,
        onDeleteAccountStateConsumed = viewModel::resetDeleteAccountState
    )
}

@Composable
private fun MyPageContent(
    uiState: MyPageUiState,
    onNavigateToGoal: () -> Unit,
    onNavigateToReminder: () -> Unit,
    onActivityToggle: (Boolean) -> Unit,
    onDeleteAccount: () -> Unit,
    deleteAccountState: DeleteAccountUiState,
    onDeleteAccountStateConsumed: () -> Unit
) {
    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    val openDeleteDialog = rememberThrottleClick(onClick = { isDeleteDialogVisible = true })
    val closeDeleteDialog = rememberThrottleClick(onClick = { isDeleteDialogVisible = false })
    val confirmDeleteDialog = rememberThrottleClick(
        onClick = {
            isDeleteDialogVisible = false
            onDeleteAccount()
        }
    )

    if (isDeleteDialogVisible) {
        AlertDialog(
            onDismissRequest = closeDeleteDialog,
            title = { Text(text = stringResource(id = R.string.mypage_delete_account_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.mypage_delete_account_confirm_desc)) },
            confirmButton = {
                TextButton(onClick = confirmDeleteDialog) {
                    Text(text = stringResource(id = R.string.mypage_delete_account_confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = closeDeleteDialog) {
                    Text(text = stringResource(id = R.string.mypage_delete_account_cancel_button))
                }
            }
        )
    }

    when (deleteAccountState) {
        DeleteAccountUiState.Success -> {
            val closeSuccessDialog = rememberThrottleClick(
                onClick = {
                    onDeleteAccountStateConsumed()
                }
            )
            AlertDialog(
                onDismissRequest = closeSuccessDialog,
                title = { Text(text = stringResource(id = R.string.mypage_delete_account_success_title)) },
                text = { Text(text = stringResource(id = R.string.mypage_delete_account_success_desc)) },
                confirmButton = {
                    TextButton(onClick = closeSuccessDialog) {
                        Text(text = stringResource(id = R.string.mypage_delete_account_confirm_button))
                    }
                }
            )
        }

        is DeleteAccountUiState.Failure -> {
            val closeErrorDialog = rememberThrottleClick(
                onClick = {
                    onDeleteAccountStateConsumed()
                }
            )
            AlertDialog(
                onDismissRequest = closeErrorDialog,
                title = { Text(text = stringResource(id = R.string.mypage_delete_account_error_title)) },
                text = {
                    Text(
                        text = stringResource(
                            id = deleteAccountErrorMessageRes(deleteAccountState.error)
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = closeErrorDialog) {
                        Text(text = stringResource(id = R.string.mypage_delete_account_confirm_button))
                    }
                }
            )
        }

        DeleteAccountUiState.Idle,
        DeleteAccountUiState.Loading -> Unit
    }

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
                        val upgradeAccountClick = rememberThrottleClick(onClick = {})
                        Text(
                            text = stringResource(id = R.string.mypage_anonymous_info_title),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(id = R.string.mypage_anonymous_info_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = upgradeAccountClick) {
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
                onActivityToggle = onActivityToggle,
                onDeleteAccount = openDeleteDialog
            )
        }
    }
}

@Composable
private fun ProfileSection(name: String?, level: String?, isAnonymous: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val displayName = if (name.isNullOrBlank()) {
            stringResource(id = R.string.mypage_default_nickname)
        } else {
            name
        }
        val displayLevel = if (level.isNullOrBlank()) {
            stringResource(id = R.string.mypage_default_level)
        } else {
            level
        }
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
                text = displayName,
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
                text = displayLevel,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun SummaryStats(uiState: MyPageUiState) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val context = LocalContext.current
        val distanceText =
            DistanceFormatter.formatDistanceKm(context, uiState.summary?.totalThisWeekKm ?: 0.0)
        val progressText =
            PercentageFormatter.formatProgress(context, uiState.summary?.progress ?: 0f)
        StatItem(
            modifier = Modifier.weight(1f),
            label = stringResource(id = R.string.mypage_summary_distance_label),
            value = stringResource(id = R.string.mypage_summary_distance_value, distanceText)
        )
        StatItem(
            modifier = Modifier.weight(1f),
            label = stringResource(id = R.string.mypage_summary_count_label),
            value = stringResource(
                id = R.string.mypage_summary_count_value,
                uiState.summary?.recordCountThisWeek ?: 0
            )
        )
        StatItem(
            modifier = Modifier.weight(1f),
            label = stringResource(id = R.string.mypage_summary_progress_label),
            value = stringResource(id = R.string.mypage_summary_progress_value, progressText)
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
    val throttledOnClick = rememberThrottleClick(onClick = onClick)
    AppContentCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(id = R.string.mypage_goal_progress_title),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = throttledOnClick) {
                    Text(text = stringResource(id = R.string.mypage_goal_progress_detail))
                }
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
    onActivityToggle: (Boolean) -> Unit,
    onDeleteAccount: () -> Unit
) {
    AppContentCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SettingItem(
                icon = Icons.Default.Notifications,
                title = stringResource(id = R.string.mypage_setting_notification_title),
                subTitle = stringResource(id = R.string.mypage_setting_notification_desc),
                onClick = onNavigateToReminder
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingItem(
                icon = Icons.Default.Edit,
                title = stringResource(id = R.string.mypage_setting_goal_title),
                subTitle = stringResource(id = R.string.mypage_setting_goal_desc),
                onClick = onNavigateToGoal
            )
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
                    Text(
                        text = stringResource(id = R.string.mypage_setting_activity_title),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(id = R.string.mypage_setting_activity_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Switch(
                    checked = uiState.isActivityRecognitionEnabled,
                    onCheckedChange = onActivityToggle
                )
            }
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = appSpacingLg()),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            SettingItem(
                icon = Icons.Default.Delete,
                title = stringResource(id = R.string.mypage_setting_delete_account_title),
                subTitle = stringResource(id = R.string.mypage_setting_delete_account_desc),
                onClick = onDeleteAccount
            )
        }
    }
}

@Composable
private fun SettingItem(icon: ImageVector, title: String, subTitle: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .throttleClick(onClick = onClick)
    ) {
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
    RunningGoalTrackerTheme {
        MyPageContent(
            uiState = MyPageUiState.preview(),
            onNavigateToGoal = {},
            onNavigateToReminder = {},
            onActivityToggle = {},
            onDeleteAccount = {},
            deleteAccountState = DeleteAccountUiState.Idle,
            onDeleteAccountStateConsumed = {}
        )
    }
}

private fun deleteAccountErrorMessageRes(error: AuthError): Int =
    when (error) {
        AuthError.NetworkError ->
            R.string.mypage_delete_account_error_desc_network

        AuthError.PermissionDenied ->
            R.string.mypage_delete_account_error_desc_permission

        AuthError.Unknown ->
            R.string.mypage_delete_account_error_desc_unknown

        AuthError.NicknameTaken ->
            R.string.mypage_delete_account_error_desc_unknown
    }
