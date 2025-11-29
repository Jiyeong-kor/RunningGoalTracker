package com.jeong.runninggoaltracker.presentation.reminder

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("러닝 알림 설정")

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { hourText = it },
                label = { Text("시 (0~23)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = minuteText,
                onValueChange = { minuteText = it },
                label = { Text("분 (0~59)") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("알림 사용")
            Switch(
                checked = state.enabled,
                onCheckedChange = { enabled -> vm.updateEnabled(enabled) }
            )
        }

        Button(
            onClick = {
                val h = hourText.toIntOrNull()
                val m = minuteText.toIntOrNull()
                if (h != null && m != null && h in 0..23 && m in 0..59) {
                    vm.updateTime(h, m)

                    if (state.enabled) {
                        // 알림 사용 ON이면 알람 예약
                        scheduler.schedule(h, m)
                    } else {
                        // 알림 사용 OFF면 알람 취소
                        scheduler.cancel(h, m)
                    }

                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장하기")
        }
    }
}
