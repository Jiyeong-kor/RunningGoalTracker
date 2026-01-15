package com.jeong.runninggoaltracker.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.jeong.runninggoaltracker.feature.auth.R
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme

@Composable
fun NicknameScreen(
    uiState: OnboardingUiState,
    modifier: Modifier = Modifier,
    onNicknameChanged: (String) -> Unit,
    onContinue: () -> Unit
) {
    val spacingSm =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_sm)
    val spacingLg =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_lg)
    val spacingXl =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_xl)
    val spacing2xl =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_2xl)
    val cornerRadius =
        dimensionResource(id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.onboarding_corner_radius)

    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacingXl, vertical = spacing2xl),
        verticalArrangement = Arrangement.spacedBy(spacingLg)
    ) {
        Text(
            text = stringResource(id = R.string.nickname_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(id = R.string.nickname_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        AppSurfaceCard(
            contentPadding = PaddingValues(vertical = spacingLg, horizontal = spacingXl)
        ) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(spacingSm)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.nickname,
                    onValueChange = onNicknameChanged,
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
                    },
                    label = { Text(text = stringResource(id = R.string.nickname_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(cornerRadius)
                )
                Text(
                    text = stringResource(id = R.string.nickname_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (uiState.nicknameErrorResId != null) {
            Text(
                text = stringResource(id = uiState.nicknameErrorResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (uiState.errorMessageResId != null) {
            Text(
                text = stringResource(id = uiState.errorMessageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(spacingSm))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onContinue,
            enabled = !uiState.isLoading,
            contentPadding = PaddingValues(vertical = spacingLg)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(spacingLg),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = dimensionResource(
                        id = com.jeong.runninggoaltracker.shared.designsystem.R.dimen.spacing_xs
                    )
                )
            } else {
                Text(
                    text = stringResource(id = R.string.nickname_continue),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NicknameScreenPreview() {
    RunningGoalTrackerTheme {
        NicknameScreen(
            uiState = OnboardingUiState(nickname = "러너"),
            onNicknameChanged = {},
            onContinue = {}
        )
    }
}
