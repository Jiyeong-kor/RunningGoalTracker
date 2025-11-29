package com.jeong.runninggoaltracker.presentation.record

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActivityLogEntry(
    val time: String,
    val label: String,
    val confidence: Int
)

object ActivityLogHolder {

    private const val MAX_SIZE = 10

    private val _logs = MutableStateFlow<List<ActivityLogEntry>>(emptyList())
    val logs: StateFlow<List<ActivityLogEntry>> = _logs

    fun add(label: String, confidence: Int) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

        val newEntry = ActivityLogEntry(
            time = time,
            label = label,
            confidence = confidence
        )

        // 최근 항목이랑 같으면 굳이 추가 안 함 (원하면 제거해도 됨)
        val current = _logs.value
        if (current.firstOrNull()?.label == label &&
            current.firstOrNull()?.confidence == confidence
        ) {
            return
        }

        _logs.value = (listOf(newEntry) + current).take(MAX_SIZE)
    }
}
