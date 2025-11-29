package com.jeong.runninggoaltracker.presentation.record

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecordScreen(
    repository: RunningRepository
) {
    val viewModel: RecordViewModel = viewModel(
        factory = RecordViewModelFactory(repository)
    )

    var distanceText by remember { mutableStateOf("") }
    var durationText by remember { mutableStateOf("") }

    val records by viewModel.records.collectAsState()

    val context = LocalContext.current
    val activityManager = remember { ActivityRecognitionManager(context.applicationContext) }
    val activityState by ActivityRecognitionStateHolder.state.collectAsState()

    // 화면에 보여줄 라벨로 변환
    val displayLabel = when (activityState.label) {
        "NO_PERMISSION" -> "권한 필요"
        "REQUEST_FAILED", "SECURITY_EXCEPTION" -> "활동 감지 실패"
        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> "알 수 없음"
        else -> activityState.label   // RUNNING, WALKING, STILL, IN_VEHICLE 등
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("현재 활동: $displayLabel (${activityState.confidence}%)")
        // 퍼센트까지 숨기고 싶으면 위 줄을 아래처럼 바꿔도 됨:
        // Text("현재 활동: $displayLabel")

        Text("러닝 기록 추가")

        OutlinedTextField(
            value = distanceText,
            onValueChange = { distanceText = it },
            label = { Text("거리 (km)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = durationText,
            onValueChange = { durationText = it },
            label = { Text("시간 (분)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val distance = distanceText.toDoubleOrNull()
                val duration = durationText.toIntOrNull()

                if (distance != null && duration != null) {
                    viewModel.addRecord(distance, duration)
                    distanceText = ""
                    durationText = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장하기")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("저장된 러닝 기록")

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records) { record ->
                Text("${record.date} - ${record.distanceKm} km / ${record.durationMinutes} 분")
            }
        }

        Button(
            onClick = {
                activityManager.startUpdates()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("활동 감지 시작 (실험)")
        }

        Button(
            onClick = {
                activityManager.stopUpdates()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("활동 감지 중지")
        }
    }
}
