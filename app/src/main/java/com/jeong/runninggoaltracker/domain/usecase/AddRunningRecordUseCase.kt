package com.jeong.runninggoaltracker.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRepository
import java.time.LocalDate
import javax.inject.Inject

class AddRunningRecordUseCase @Inject constructor(
    private val repository: RunningRepository
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(distanceKm: Double, durationMinutes: Int) {
        val today = LocalDate.now().toString()
        repository.addRecord(
            RunningRecord(
                date = today,
                distanceKm = distanceKm,
                durationMinutes = durationMinutes
            )
        )
    }
}
