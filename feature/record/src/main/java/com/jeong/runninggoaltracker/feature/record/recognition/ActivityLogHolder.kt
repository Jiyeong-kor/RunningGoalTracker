package com.jeong.runninggoaltracker.feature.record.recognition

import com.jeong.runninggoaltracker.feature.record.api.model.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityRecognitionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ActivityLogHolder {

    private const val MAX_SIZE = 10

    private val _logs = MutableStateFlow<List<ActivityLogEntry>>(emptyList())
    val logs: StateFlow<List<ActivityLogEntry>> = _logs

    fun add(status: ActivityRecognitionStatus) {
        val timestamp = System.currentTimeMillis()

        val current = _logs.value
        if (current.firstOrNull()?.status == status) return

        val newEntry = ActivityLogEntry(time = timestamp, status = status)
        _logs.value = (listOf(newEntry) + current).take(MAX_SIZE)
    }
}
