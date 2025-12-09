package com.jeong.runninggoaltracker.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class RunningSummary(
    val weeklyGoalKm: Double? = null,
    val totalThisWeekKm: Double = 0.0,
    val recordCountThisWeek: Int = 0,
    val progress: Float = 0f
)

@RequiresApi(Build.VERSION_CODES.O)
class GetRunningSummaryUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    operator fun invoke(): Flow<RunningSummary> {
        return combine(
            repository.getGoal(),
            repository.getAllRecords()
        ) { goal, records ->
            buildSummary(goal, records)
        }
    }

    private fun buildSummary(goal: RunningGoal?, records: List<RunningRecord>): RunningSummary {
        val startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY)

        val thisWeekRecords = records.filter { record ->
            val date = runCatching { LocalDate.parse(record.date) }.getOrNull()
            date != null && !date.isBefore(startOfWeek)
        }

        val totalKm = thisWeekRecords.sumOf { it.distanceKm }
        val count = thisWeekRecords.size
        val weeklyGoalKm = goal?.weeklyGoalKm

        val progress = if (weeklyGoalKm != null && weeklyGoalKm > 0.0) {
            (totalKm / weeklyGoalKm)
                .coerceIn(0.0, 1.0)
                .toFloat()
        } else {
            0f
        }

        return RunningSummary(
            weeklyGoalKm = weeklyGoalKm,
            totalThisWeekKm = totalKm,
            recordCountThisWeek = count,
            progress = progress
        )
    }
}
