package com.jeong.runninggoaltracker.feature.auth.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.auth.R
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        viewModel.onPermissionsResult(results.values.all { it })
    }

    val permissionList = remember { buildOnboardingPermissions() }

    when (uiState.step) {
        OnboardingStep.Permissions -> PermissionsScreen(
            modifier = modifier,
            permissionErrorResId = uiState.permissionErrorResId,
            onAgree = { permissionLauncher.launch(permissionList) }
        )

        OnboardingStep.Nickname -> NicknameScreen(
            modifier = modifier,
            uiState = uiState,
            onNicknameChanged = viewModel::onNicknameChanged,
            onContinue = viewModel::onContinueWithNickname
        )

        OnboardingStep.Success -> SuccessScreen(
            modifier = modifier,
            onContinue = onComplete
        )
    }

    if (uiState.showNoInternetDialog) {
        NoInternetDialog(
            onRetry = viewModel::onRetryInternet,
            onDismiss = viewModel::onDismissNoInternetDialog
        )
    }
}

private fun buildOnboardingPermissions(): Array<String> =
    buildList {
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

@Composable
private fun PermissionsScreen(
    modifier: Modifier = Modifier,
    permissionErrorResId: Int?,
    onAgree: () -> Unit
) {
    val spacingSm =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_sm)
    val spacingMd =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_md)
    val spacingLg =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_lg)
    val spacingXl =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_xl)
    val spacing2xl =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_2xl)
    val onAgreeThrottled = rememberThrottleClick(onClick = onAgree)

    val permissions = listOf(
        PermissionItem(
            icon = Icons.AutoMirrored.Outlined.DirectionsRun,
            title = stringResource(id = R.string.permission_activity_title),
            description = stringResource(id = R.string.permission_activity_description),
            isEssential = true
        ),
        PermissionItem(
            icon = Icons.Outlined.LocationOn,
            title = stringResource(id = R.string.permission_location_title),
            description = stringResource(id = R.string.permission_location_description),
            isEssential = true
        ),
        PermissionItem(
            icon = Icons.Outlined.PhotoCamera,
            title = stringResource(id = R.string.permission_camera_title),
            description = stringResource(id = R.string.permission_camera_description),
            isEssential = true
        ),
        PermissionItem(
            icon = Icons.Outlined.Notifications,
            title = stringResource(id = R.string.permission_notification_title),
            description = stringResource(id = R.string.permission_notification_description),
            isEssential = true
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacingXl, vertical = spacing2xl)
    ) {
        HeaderIcon()
        Spacer(modifier = Modifier.height(spacingMd))
        Text(
            text = stringResource(id = R.string.permission_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(spacingSm))
        Text(
            text = stringResource(id = R.string.permission_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(spacingLg))
        permissions.forEach { permission ->
            AppContentCard {
                PermissionRow(
                    item = permission,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(spacingMd))
        }

        if (permissionErrorResId != null) {
            Text(
                text = stringResource(id = permissionErrorResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(spacingMd))
        }

        Spacer(modifier = Modifier.height(spacingLg))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAgreeThrottled,
            contentPadding = PaddingValues(vertical = spacingLg)
        ) {
            Text(
                text = stringResource(id = R.string.permission_agree),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun PermissionRow(
    item: PermissionItem,
    modifier: Modifier = Modifier
) {
    val spacingSm =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_sm)
    val spacingMd =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_md)
    val tagCornerRadius =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_tag_corner_radius)
    val tagPaddingHorizontal =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_tag_padding_horizontal)
    val tagPaddingVertical =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_tag_padding_vertical)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacingSm)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.isEssential) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(tagCornerRadius)
                    ) {
                        Text(
                            text = stringResource(id = R.string.permission_essential),
                            modifier = Modifier.padding(
                                horizontal = tagPaddingHorizontal,
                                vertical = tagPaddingVertical
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacingSm))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SuccessScreen(
    modifier: Modifier = Modifier,
    onContinue: () -> Unit
) {
    val spacingLg =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_lg)
    val spacingXl =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_xl)
    val spacing2xl =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_2xl)
    val iconSize =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_icon_size)
    val onContinueThrottled = rememberThrottleClick(onClick = onContinue)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = spacingXl, vertical = spacing2xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.DirectionsRun,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(iconSize)
        )
        Spacer(modifier = Modifier.height(spacingLg))
        Text(
            text = stringResource(id = R.string.success_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(spacingLg))
        Text(
            text = stringResource(id = R.string.success_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onContinueThrottled,
            contentPadding = PaddingValues(vertical = spacingLg)
        ) {
            Text(
                text = stringResource(id = R.string.success_continue),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

private data class PermissionItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val isEssential: Boolean
)

@Composable
private fun HeaderIcon() {
    val cornerRadius =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_corner_radius)
    val iconContainerSize =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_icon_container_size)
    val iconSize =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_icon_size)
    val cardElevation =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_card_elevation)

    Card(
        modifier = Modifier.size(iconContainerSize),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.DirectionsRun,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
private fun NoInternetDialog(
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val spacingSm =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_sm)
    val spacingMd =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_md)
    val spacingLg =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_lg)
    val spacingXl =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_xl)
    val onRetryThrottled = rememberThrottleClick(onClick = onRetry)

    Dialog(onDismissRequest = onDismiss) {
        AppSurfaceCard(
            modifier = Modifier.padding(horizontal = spacingXl)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacingMd)) {
                Text(
                    text = stringResource(id = R.string.no_internet_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(id = R.string.no_internet_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(spacingSm))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRetryThrottled,
                    contentPadding = PaddingValues(vertical = spacingLg)
                ) {
                    Text(
                        text = stringResource(id = R.string.no_internet_retry),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PermissionsScreenPreview() {
    RunningGoalTrackerTheme {
        PermissionsScreen(
            permissionErrorResId = null,
            onAgree = {}
        )
    }
}
