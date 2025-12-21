package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class AddRunningRecordUseCaseTest {

    private val repository = FakeRunningRecordRepository()
    private val useCase = AddRunningRecordUseCase(repository)

    @Test
    fun `제공된 값으로 리포지토리에 위임`() = runBlocking {
        val date = LocalDate.of(2024, 6, 1)

        useCase(
            date = date,
            distanceKm = 7.5,
            durationMinutes = 42
        )

        assertEquals(1, repository.added.size)
        val record = repository.added.first()
        assertEquals(date, record.date)
        assertEquals(7.5, record.distanceKm, 0.0)
        assertEquals(42, record.durationMinutes)
    }

    private class FakeRunningRecordRepository : RunningRecordRepository {
        val added = mutableListOf<RunningRecord>()
        private val records = MutableStateFlow<List<RunningRecord>>(emptyList())

        override fun getAllRecords(): Flow<List<RunningRecord>> = records

        override suspend fun addRecord(record: RunningRecord) {
            added += record
            records.value = records.value + record
        }
    }
}
