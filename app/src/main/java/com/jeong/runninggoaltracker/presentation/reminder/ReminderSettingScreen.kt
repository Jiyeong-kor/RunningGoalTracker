package com.jeong.runninggoaltracker.presentation.reminder

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jeong.runninggoaltracker.domain.repository.RunningRepository

@SuppressLint("ScheduleExactAlarm")
@Composable
fun ReminderSettingScreen(
    repository: RunningRepository,
    onBack: () -> Unit
) {
    val vm: ReminderViewModel = viewModel(
        factory = ReminderViewModelFactory(repository)
    )

    val state by vm.uiState.collectAsState()
    val context = LocalContext.current
    val scheduler = remember { ReminderAlarmScheduler(context) }

    var hourText by remember { mutableStateOf(state.hour.toString()) }
    var minuteText by remember { mutableStateOf(state.minute.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "러닝 알림 설정",
            style = typography.titleLarge
        )

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val currentTimeLabel = "%02d:%02d".format(state.hour, state.minute)
                Text(
                    text = "현재 알림 시각: $currentTimeLabel",
                    style = typography.bodyLarge
                )
                Text(
                    text = if (state.enabled) "알림 사용: 켜짐" else "알림 사용: 꺼짐",
                    style = typography.bodyMedium,
                    color = colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = {
                            hourText = it
                            errorText = null
                        },
                        label = { Text("시 (0~23)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = {
                            minuteText = it
                            errorText = null
                        },
                        label = { Text("분 (0~59)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("알림 사용", style = typography.bodyLarge)
                    Switch(
                        checked = state.enabled,
                        onCheckedChange = { enabled ->
                            vm.updateEnabled(enabled)
                        }
                    )
                }

                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        color = colorScheme.error,
                        style = typography.bodyMedium
                    )
                }

                Button(
                    onClick = {
                        val h = hourText.toIntOrNull()
                        val m = minuteText.toIntOrNull()

                        when {
                            h == null || m == null -> {
                                errorText = "시/분을 숫자로 입력해주세요."
                            }

                            h !in 0..23 || m !in 0..59 -> {
                                errorText = "시(0~23), 분(0~59) 범위로 입력해주세요."
                            }

                            else -> {
                                vm.updateTime(h, m)
                                if (state.enabled) {
                                    scheduler.schedule(h, m)
                                } else {
                                    scheduler.cancel(h, m)
                                }
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
