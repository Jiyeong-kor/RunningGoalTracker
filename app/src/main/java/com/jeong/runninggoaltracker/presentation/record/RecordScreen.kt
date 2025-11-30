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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
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
    var errorText by remember { mutableStateOf<String?>(null) }

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

    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "러닝 기록",
            style = typography.titleLarge
        )

        // 카드 1: 활동 인식
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "활동 인식",
                    style = typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                        contentDescription = null,
                        tint = colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "현재 활동: $displayLabel",
                            style = typography.bodyLarge
                        )
                        Text(
                            text = "신뢰도: ${activityState.confidence}%",
                            style = typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

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
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "러닝 기록 추가",
                    style = typography.titleMedium
                )

                OutlinedTextField(
                    value = distanceText,
                    onValueChange = {
                        distanceText = it
                        errorText = null
                    },
                    label = { Text("거리 (km)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = durationText,
                    onValueChange = {
                        durationText = it
                        errorText = null
                    },
                    label = { Text("시간 (분)") },
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
                        val distance = distanceText.toDoubleOrNull()
                        val duration = durationText.toIntOrNull()

                        when {
                            distance == null || duration == null -> {
                                errorText = "숫자 형식으로 입력해주세요."
                            }
                            distance <= 0.0 || duration <= 0 -> {
                                errorText = "0보다 큰 값을 입력해주세요."
                            }
                            else -> {
                                viewModel.addRecord(distance, duration)
                                distanceText = ""
                                durationText = ""
                                errorText = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("저장하기")
                }
            }
        }

        // 카드 3: 저장된 기록
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surfaceContainerLow
            ),
            elevation = CardDefaults.cardElevation(1.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "저장된 러닝 기록",
                    style = typography.titleMedium
                )

                if (records.isEmpty()) {
                    Text(
                        text = "저장된 기록이 없습니다.",
                        style = typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(records) { record ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = colorScheme.surfaceContainer
                                ),
                                elevation = CardDefaults.cardElevation(0.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                                        contentDescription = null,
                                        tint = colorScheme.primary
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.CalendarToday,
                                                contentDescription = null,
                                                tint = colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = record.date,
                                                style = typography.bodyMedium
                                            )
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "${record.distanceKm} km",
                                                style = typography.bodyMedium
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Schedule,
                                                    contentDescription = null,
                                                    tint = colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${record.durationMinutes} 분",
                                                    style = typography.bodyMedium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
