package com.jeong.runninggoaltracker.presentation.goal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
        mutableStateOf(
            state.currentGoalKm?.toString().orEmpty()
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("주간 목표 설정")

        if (state.currentGoalKm != null) {
            Text("현재 목표: ${state.currentGoalKm} km")
        } else {
            Text("현재 설정된 목표가 없습니다.")
        }

        OutlinedTextField(
            value = goalText,
            onValueChange = { goalText = it },
            label = { Text("주간 목표 거리 (km)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val goal = goalText.toDoubleOrNull()
                if (goal != null) {
                    viewModel.saveGoal(goal)
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장하기")
        }
    }
}
