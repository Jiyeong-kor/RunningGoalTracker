package com.jeong.runninggoaltracker.presentation.reminder

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    // TimePicker에서 선택된 값을 임시 저장하기 위한 상태
    var hourText by remember { mutableStateOf(state.hour.toString()) }
    var minuteText by remember { mutableStateOf(state.minute.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 저장된 시간 표시
                val currentTimeLabel = "%02d:%02d".format(state.hour, state.minute)
                Text(
                    text = "현재 알림 시각: $currentTimeLabel",
                    style = typography.bodyLarge
                )

                // TimePicker 호출 버튼
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(text = "알림 시간 선택: $currentTimeLabel", style = typography.bodyLarge) }
                }

                // 알림 활성화 스위치
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

                // 저장 버튼
                Button(
                    onClick = {
                        val h = hourText.toIntOrNull()
                        val m = minuteText.toIntOrNull()

                        if (h != null && m != null) {
                            vm.updateTime(h, m)

                            if (state.enabled) {
                                scheduler.schedule(h, m)
                            } else {
                                scheduler.cancel(h, m)
                            }
                            onBack()
                        } else {
                            errorText = "시간 선택을 확인해주세요."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("저장하기")
                }
            }
        }

        @OptIn(ExperimentalMaterial3Api::class)
        if (showTimePicker) {
            val timeState = rememberTimePickerState(
                initialHour = hourText.toIntOrNull() ?: state.hour, // 현재 저장된 시간 사용
                initialMinute = minuteText.toIntOrNull() ?: state.minute
            )
            TimePickerDialog(
                onDismissRequest = { showTimePicker = false },

                confirmButton = {
                    Button(onClick = {
                        hourText = timeState.hour.toString()
                        minuteText = timeState.minute.toString()
                        errorText = null
                        showTimePicker = false
                    }) {
                        Text("확인")
                    }
                },

                dismissButton = {
                    OutlinedButton(onClick = { showTimePicker = false }) {
                        Text("취소")
                    }
                },

                title = { Text("알림 시간 선택") }
            ) {
                TimePicker(state = timeState)
            }
        }
    }
}