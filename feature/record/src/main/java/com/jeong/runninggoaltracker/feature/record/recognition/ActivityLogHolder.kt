package com.jeong.runninggoaltracker.feature.record.recognition

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ActivityLogEntry(
    val time: Long,
    val label: String
)

object ActivityLogHolder {

    private const val MAX_SIZE = 10

    private val _logs = MutableStateFlow<List<ActivityLogEntry>>(emptyList())
    val logs: StateFlow<List<ActivityLogEntry>> = _logs

    fun add(label: String) {
        val timestamp = System.currentTimeMillis()

        val current = _logs.value
        if (current.firstOrNull()?.label == label) return

        val newEntry = ActivityLogEntry(time = timestamp, label = label)
        _logs.value = (listOf(newEntry) + current).take(MAX_SIZE)
    }
}
