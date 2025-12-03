package com.jeong.runninggoaltracker.presentation.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeong.runninggoaltracker.domain.repository.RunningRepository

@Composable
fun GoalSettingScreen(
    repository: RunningRepository,
    onBack: () -> Unit
) {
    val viewModel: GoalViewModel = viewModel(
        factory = GoalViewModelFactory(repository)
    )

    val state by viewModel.uiState.collectAsState()

    var goalText by remember {
        mutableStateOf(state.currentGoalKm?.toString().orEmpty())
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.currentGoalKm != null) {
                    Text(
                        text = "현재 목표: ${"%.1f".format(state.currentGoalKm)} km",
                        style = typography.bodyLarge
                    )
                } else {
                    Text(
                        text = "현재 설정된 목표가 없습니다.",
                        style = typography.bodyLarge
                    )
                }

                OutlinedTextField(
                    value = goalText,
                    onValueChange = {
                        goalText = it
                        errorText = null
                    },
                    label = { Text("주간 목표 거리 (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = colorScheme.error,
                        style = typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        val goal = goalText.toDoubleOrNull()
                        when {
                            goal == null -> {
                                errorText = "숫자를 입력해주세요."
                            }
                            goal <= 0.0 -> {
                                errorText = "0보다 큰 값을 입력해주세요."
                            }
                            else -> {
                                viewModel.saveGoal(goal)
                                onBack()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("저장하기")
                }
            }
        }
    }
}
