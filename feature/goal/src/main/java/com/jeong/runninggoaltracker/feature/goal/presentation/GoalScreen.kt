package com.jeong.runninggoaltracker.feature.goal.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.jeong.runninggoaltracker.feature.goal.R
import com.jeong.runninggoaltracker.shared.designsystem.R as SharedR
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard

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
    onGoalChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val errorText = when (state.error) {
        GoalInputError.INVALID_NUMBER -> stringResource(R.string.error_enter_number)
        GoalInputError.NON_POSITIVE -> stringResource(R.string.error_enter_positive_value)
        null -> null
    }

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = dimensionResource(SharedR.dimen.padding_screen_horizontal),
                vertical = dimensionResource(SharedR.dimen.padding_screen_vertical)
            ),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(SharedR.dimen.spacing_screen_elements)
        )
    ) {
        AppContentCard {
            if (state.currentGoalKm != null) {
                Text(
                    text = stringResource(
                        R.string.goal_current_format,
                        state.currentGoalKm
                    ),
                    style = typography.bodyLarge
                )
            } else {
                Text(
                    text = stringResource(R.string.goal_no_current_goal),
                    style = typography.bodyLarge
                )
            }

            OutlinedTextField(
                value = state.weeklyGoalInput,
                onValueChange = onGoalChange,
                label = { Text(stringResource(R.string.goal_weekly_distance_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_input"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            if (errorText != null) {
                Text(
                    text = errorText,
                    color = colorScheme.error,
                    style = typography.bodyMedium
                )
            }

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_save))
            }
        }
    }
}
