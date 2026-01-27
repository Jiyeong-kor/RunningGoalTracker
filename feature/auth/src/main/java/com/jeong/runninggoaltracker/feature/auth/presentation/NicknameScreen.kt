package com.jeong.runninggoaltracker.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.jeong.runninggoaltracker.feature.auth.R
import com.jeong.runninggoaltracker.feature.auth.contract.PrivacyPolicyContract
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingSm

@Composable
fun NicknameScreen(
    uiState: OnboardingUiState,
    isPrivacyAccepted: Boolean,
    onNicknameChanged: (String) -> Unit,
    onPrivacyAcceptedChange: (Boolean) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
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
    val uriHandler = LocalUriHandler.current
    val privacyPolicyLabel = stringResource(id = R.string.privacy_policy_agreement_link)
    val privacyPolicyLink = LinkAnnotation.Url(PrivacyPolicyContract.PRIVACY_POLICY_URL) {
        uriHandler.openUri(PrivacyPolicyContract.PRIVACY_POLICY_URL)
    }
    val privacyPolicyText = buildAnnotatedString {
        append(stringResource(id = R.string.privacy_policy_agreement_prefix))
        pushLink(privacyPolicyLink)
        pushStyle(
            SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        )
        append(privacyPolicyLabel)
        pop()
        pop()
        append(stringResource(id = R.string.privacy_policy_agreement_suffix))
    }
    val privacyPolicyAccessibilityLabel =
        stringResource(id = R.string.privacy_policy_agreement_full)

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
                    enabled = !uiState.isLoading,
                    isError = uiState.nicknameValidationMessage != null || uiState.nicknameHintError,
                    leadingIcon = {
                        Icon(imageVector = Icons.Outlined.Person, contentDescription = null)
                    },
                    label = { Text(text = stringResource(id = R.string.nickname_label)) },
                    singleLine = true,
                    shape = RoundedCornerShape(cornerRadius)
                )
                Row(modifier = Modifier.padding(top = appSpacingSm())) {
                    Text(
                        text = stringResource(id = R.string.anonymous_nickname_caption),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = stringResource(id = R.string.nickname_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (uiState.nicknameHintError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        if (uiState.nicknameValidationMessage != null) {
            Text(
                text = stringResource(id = uiState.nicknameValidationMessage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        if (uiState.nicknameAvailabilityMessageResId != null) {
            Text(
                text = stringResource(id = uiState.nicknameAvailabilityMessageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (uiState.errorMessageResId != null) {
            Text(
                text = stringResource(id = uiState.errorMessageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacingSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isPrivacyAccepted,
                onCheckedChange = onPrivacyAcceptedChange,
                enabled = !uiState.isLoading,
                modifier = Modifier.semantics {
                    contentDescription = privacyPolicyAccessibilityLabel
                }
            )
            Text(
                text = privacyPolicyText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .padding(top = spacingSm)
                    .semantics {
                        contentDescription = privacyPolicyAccessibilityLabel
                    }
            )
        }
        val onContinueThrottled = rememberThrottleClick(onClick = onContinue)
        Spacer(modifier = Modifier.height(spacingSm))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onContinueThrottled,
            enabled = uiState.isNicknameValid && isPrivacyAccepted && !uiState.isLoading,
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
private fun NicknameScreenPreview() = RunningGoalTrackerTheme {
    NicknameScreen(
        uiState = previewUiState(),
        isPrivacyAccepted = false,
        onNicknameChanged = {},
        onPrivacyAcceptedChange = {},
        onContinue = {}
    )
}

@Composable
private fun previewUiState(): OnboardingUiState =
    OnboardingUiState(nickname = stringResource(id = R.string.nickname_preview_value))
