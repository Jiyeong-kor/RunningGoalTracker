package com.jeong.runninggoaltracker.feature.goal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.goal.R
import com.jeong.runninggoaltracker.feature.goal.contract.GOAL_MIN_KM
import com.jeong.runninggoaltracker.feature.goal.contract.GOAL_PRESET_BASIC_FITNESS_KM
import com.jeong.runninggoaltracker.feature.goal.contract.GOAL_PRESET_HEALTH_MAINTAIN_KM
import com.jeong.runninggoaltracker.feature.goal.contract.GOAL_PRESET_LIGHT_WALK_KM
import com.jeong.runninggoaltracker.feature.goal.contract.GOAL_STEP_KM
import com.jeong.runninggoaltracker.shared.designsystem.formatter.DistanceFormatter
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.extension.throttleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.appAccentColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appBackgroundColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextMutedColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextPrimaryColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme

@Composable
fun GoalRoute(
    onBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    GoalScreen(
        state = state,
        onGoalChange = viewModel::onWeeklyGoalChanged,
        onSave = { viewModel.saveGoal(onBack) }
    )
}

@Composable
fun GoalScreen(
    state: GoalUiState,
    onGoalChange: (Double) -> Unit,
    onSave: () -> Unit
) {
    val errorText = when (state.error) {
        GoalInputError.INVALID_NUMBER -> stringResource(R.string.error_enter_number)
        GoalInputError.NON_POSITIVE -> stringResource(R.string.error_enter_positive_value)
        null -> null
    }

    val goalDistance = state.weeklyGoalKmInput
        ?: state.currentGoalKm
        ?: GOAL_MIN_KM
    val context = LocalContext.current
    val goalDistanceLabel = DistanceFormatter.formatDistanceKm(context, goalDistance)

    val onSaveThrottled = rememberThrottleClick(onClick = onSave)
    val onDecreaseThrottled = rememberThrottleClick {
        val nextValue = (goalDistance - GOAL_STEP_KM).coerceAtLeast(GOAL_MIN_KM)
        onGoalChange(nextValue)
    }
    val onIncreaseThrottled = rememberThrottleClick {
        val nextValue = goalDistance + GOAL_STEP_KM
        onGoalChange(nextValue)
    }
    val accentColor = appAccentColor()
    val backgroundColor = appBackgroundColor()
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()
    val screenPadding = dimensionResource(R.dimen.goal_screen_padding)
    val titleTopSpacing = dimensionResource(R.dimen.goal_title_top_spacing)
    val titleBottomSpacing = dimensionResource(R.dimen.goal_title_bottom_spacing)
    val adjustButtonSpacing = dimensionResource(R.dimen.goal_adjust_button_spacing)
    val errorSpacing = dimensionResource(R.dimen.goal_error_spacing)
    val presetSectionTopSpacing = dimensionResource(R.dimen.goal_preset_section_top_spacing)
    val presetSectionBottomSpacing = dimensionResource(R.dimen.goal_preset_section_bottom_spacing)
    val presetItemSpacing = dimensionResource(R.dimen.goal_preset_item_spacing)
    val saveButtonHeight = dimensionResource(R.dimen.goal_save_button_height)
    val saveButtonCornerRadius = dimensionResource(R.dimen.goal_save_button_corner_radius)
    val titleTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.goal_text_size_title).toSp()
    }
    val valueTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.goal_text_size_value).toSp()
    }
    val unitTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.goal_text_size_unit).toSp()
    }
    val errorTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.goal_text_size_error).toSp()
    }
    val sectionTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.goal_text_size_section).toSp()
    }
    val buttonTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.goal_text_size_button).toSp()
    }
    val fillWeight = integerResource(R.integer.goal_weight_full).toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(titleTopSpacing))

        Text(
            stringResource(R.string.goal_title_weekly_distance),
            color = textMuted,
            fontSize = titleTextSize
        )

        Spacer(modifier = Modifier.height(titleBottomSpacing))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(adjustButtonSpacing)
        ) {
            GoalAdjustButton(
                icon = Icons.Default.Remove,
                contentDescription = stringResource(R.string.goal_action_decrease),
                onClick = onDecreaseThrottled
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = goalDistanceLabel,
                    color = textPrimary,
                    fontSize = valueTextSize,
                    fontWeight = FontWeight.Black
                )
                Text(
                    stringResource(R.string.goal_unit_km),
                    color = accentColor,
                    fontSize = unitTextSize,
                    fontWeight = FontWeight.Bold
                )
            }

            GoalAdjustButton(
                icon = Icons.Default.Add,
                contentDescription = stringResource(R.string.goal_action_increase),
                onClick = onIncreaseThrottled
            )
        }

        if (errorText != null) {
            Spacer(modifier = Modifier.height(errorSpacing))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                fontSize = errorTextSize,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(presetSectionTopSpacing))

        Text(
            stringResource(R.string.goal_preset_section_title),
            modifier = Modifier.align(Alignment.Start),
            color = textMuted,
            fontSize = sectionTextSize,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(presetSectionBottomSpacing))

        Column(verticalArrangement = Arrangement.spacedBy(presetItemSpacing)) {
            val presets = listOf(
                stringResource(R.string.goal_preset_light_walk) to GOAL_PRESET_LIGHT_WALK_KM,
                stringResource(R.string.goal_preset_basic_fitness) to GOAL_PRESET_BASIC_FITNESS_KM,
                stringResource(R.string.goal_preset_health_maintain) to GOAL_PRESET_HEALTH_MAINTAIN_KM
            )
            presets.forEach { (label, value) ->
                PresetCard(label = label, isSelected = goalDistance == value) {
                    onGoalChange(value)
                }
            }
        }

        Spacer(modifier = Modifier.weight(fillWeight))

        Button(
            onClick = onSaveThrottled,
            modifier = Modifier
                .fillMaxWidth()
                .height(saveButtonHeight),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
            shape = RoundedCornerShape(saveButtonCornerRadius)
        ) {
            Text(
                stringResource(R.string.goal_save_button),
                fontSize = buttonTextSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GoalAdjustButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    val textPrimary = appTextPrimaryColor()
    val onClickThrottled = rememberThrottleClick(onClick = onClick)
    val buttonSize = dimensionResource(R.dimen.goal_adjust_button_size)
    val borderWidth = dimensionResource(R.dimen.goal_adjust_button_border_width)
    val alphaBase = integerResource(R.integer.goal_alpha_base_percent).toFloat()
    val borderAlpha = integerResource(R.integer.goal_alpha_border_percent).toFloat() / alphaBase

    Surface(
        onClick = onClickThrottled,
        modifier = Modifier.size(buttonSize),
        shape = CircleShape,
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            borderWidth,
            Color.White.copy(alpha = borderAlpha)
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = contentDescription, tint = textPrimary)
        }
    }
}

@Composable
private fun PresetCard(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val accentColor = appAccentColor()
    val textPrimary = appTextPrimaryColor()
    val cornerRadius = dimensionResource(R.dimen.goal_preset_card_corner_radius)
    val borderWidth = dimensionResource(R.dimen.goal_preset_card_border_width)
    val padding = dimensionResource(R.dimen.goal_preset_card_padding)
    val alphaBase = integerResource(R.integer.goal_alpha_base_percent).toFloat()
    val selectedAlpha = integerResource(R.integer.goal_alpha_selected_background_percent)
        .toFloat() / alphaBase
    val unselectedAlpha = integerResource(R.integer.goal_alpha_unselected_background_percent)
        .toFloat() / alphaBase
    val labelTextSize = with(LocalDensity.current) {
        dimensionResource(R.dimen.goal_text_size_section).toSp()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                role = Role.RadioButton
                selected = isSelected
            }
            .throttleClick(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        color = if (isSelected) {
            accentColor.copy(alpha = selectedAlpha)
        } else {
            Color.White.copy(alpha = unselectedAlpha)
        },
        border = if (isSelected) androidx.compose.foundation.BorderStroke(
            borderWidth,
            accentColor
        ) else null
    ) {
        Text(
            label,
            modifier = Modifier.padding(padding),
            color = if (isSelected) accentColor else textPrimary,
            fontSize = labelTextSize,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GoalScreenPreview() {
    val state = GoalUiState(
        currentGoalKm = 15.0,
        weeklyGoalKmInput = 15.0,
        error = null
    )

    RunningGoalTrackerTheme {
        GoalScreen(
            state = state,
            onGoalChange = { _ -> },
            onSave = {}
        )
    }
}
