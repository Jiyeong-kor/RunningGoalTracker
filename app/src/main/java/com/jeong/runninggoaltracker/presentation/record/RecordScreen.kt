package com.jeong.runninggoaltracker.presentation.record

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

    val displayLabel = when (activityState.label) {
        "NO_PERMISSION" -> "권한 필요"
        "REQUEST_FAILED", "SECURITY_EXCEPTION" -> "활동 감지 실패"
        "NO_RESULT", "NO_ACTIVITY", "UNKNOWN" -> "알 수 없음"
        else -> activityState.label
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("러닝 기록")

        // 카드 1: 활동 인식
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("활동 인식")
                Text("현재 활동: $displayLabel (${activityState.confidence}%)")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { activityManager.startUpdates() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("활동 감지 시작")
                    }
                    Button(
                        onClick = { activityManager.stopUpdates() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("활동 감지 중지")
                    }
                }
            }
        }

        // 카드 2: 러닝 기록 추가
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
            }
        }

        // 카드 3: 저장된 러닝 기록 리스트
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("저장된 러닝 기록")

                if (records.isEmpty()) {
                    Text("저장된 기록이 없습니다.")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(records) { record ->
                            Text("${record.date} - ${record.distanceKm} km / ${record.durationMinutes} 분")
                        }
                    }
                }
            }
        }
    }
}
