package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.model.RunningSummary
import com.jeong.runninggoaltracker.domain.repository.RunningGoalRepository
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import com.jeong.runninggoaltracker.domain.util.DateProvider
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetRunningSummaryUseCaseTest {

    private val goalRepository = FakeRunningGoalRepository()
    private val recordRepository = FakeRunningRecordRepository()
    private val dateProvider = FakeDateProvider(LocalDate.of(2024, 12, 1))
    private val summaryCalculator = FakeRunningSummaryCalculator()
    private val useCase = GetRunningSummaryUseCase(
        goalRepository = goalRepository,
        recordRepository = recordRepository,
        dateProvider = dateProvider,
        summaryCalculator = summaryCalculator
    )

    @Test
    fun `최신 목표와 기록 및 오늘 날짜 기준으로 summary emit`() = runBlocking {
        goalRepository.upsertGoal(RunningGoal(weeklyGoalKm = 25.0))
        recordRepository.addRecord(
            RunningRecord(
                date = LocalDate.of(2024, 11, 25),
                distanceKm = 6.0,
                durationMinutes = 30
            )
        )
        recordRepository.addRecord(
            RunningRecord(
                date = LocalDate.of(2024, 11, 26),
                distanceKm = 4.0,
                durationMinutes = 25
            )
        )

        val summary = useCase().first()

        assertEquals(25.0, summary.weeklyGoalKm!!, 0.0)
        assertEquals(10.0, summary.totalThisWeekKm, 0.0)
        assertEquals(2, summary.recordCountThisWeek)
        assertEquals(2f, summary.progress)
        assertEquals(
            listOf(
                FakeRunningSummaryCalculator.Invocation(
                    goal = RunningGoal(weeklyGoalKm = 25.0),
                    records = recordRepository.state.value,
                    today = LocalDate.of(2024, 12, 1)
                )
            ),
            summaryCalculator.invocations
        )
    }

    @Test
    fun `목표나 기록 변경 시 summary 업데이트`() = runBlocking {
        val summaries = mutableListOf<RunningSummary>()

        val collectionJob = launch {
            useCase().take(2).toList(summaries)
        }

        goalRepository.upsertGoal(RunningGoal(weeklyGoalKm = 12.0))
        recordRepository.addRecord(
            RunningRecord(
                date = LocalDate.of(2024, 11, 30),
                distanceKm = 12.0,
                durationMinutes = 60
            )
        )

        collectionJob.join()

        assertEquals(0.0, summaries.first().totalThisWeekKm, 0.0)
        assertEquals(12.0, summaries.last().totalThisWeekKm, 0.0)
        assertEquals(1, summaries.last().recordCountThisWeek)
        assertEquals(1f, summaries.last().progress)
    }

    @Test
    fun `새로운 날짜에서도 계산기 호출`() = runBlocking {
        val summaries = mutableListOf<RunningSummary>()

        val collectionJob = launch {
            useCase().take(2).toList(summaries)
        }

        goalRepository.upsertGoal(RunningGoal(weeklyGoalKm = 5.0))
        dateProvider.today = LocalDate.of(2024, 12, 2)
        recordRepository.addRecord(
            RunningRecord(
                date = LocalDate.of(2024, 12, 2),
                distanceKm = 3.0,
                durationMinutes = 20
            )
        )

        collectionJob.join()

        assertEquals(
            listOf(
                LocalDate.of(2024, 12, 1),
                LocalDate.of(2024, 12, 2)
            ),
            summaryCalculator.invocations.map { it.today }
        )
        assertTrue(summaries.last().progress > 0f)
    }

    private class FakeRunningGoalRepository : RunningGoalRepository {
        val state = MutableStateFlow<RunningGoal?>(null)

        override fun getGoal(): Flow<RunningGoal?> = state

        override suspend fun upsertGoal(goal: RunningGoal) {
            state.value = goal
        }
    }

    private class FakeRunningRecordRepository : RunningRecordRepository {
        val state = MutableStateFlow<List<RunningRecord>>(emptyList())

        override fun getAllRecords(): Flow<List<RunningRecord>> = state

        override suspend fun addRecord(record: RunningRecord) {
            state.value += record
        }
    }

    private class FakeDateProvider(var today: LocalDate) : DateProvider {
        override fun getToday(): LocalDate = today
    }

    private class FakeRunningSummaryCalculator : RunningSummaryCalculator {
        data class Invocation(
            val goal: RunningGoal?,
            val records: List<RunningRecord>,
            val today: LocalDate
        )

        val invocations = mutableListOf<Invocation>()

        override fun calculate(
            goal: RunningGoal?,
            records: List<RunningRecord>,
            today: LocalDate
        ): RunningSummary {
            invocations += Invocation(goal, records, today)

            return RunningSummary(
                weeklyGoalKm = goal?.weeklyGoalKm,
                totalThisWeekKm = records.sumOf { it.distanceKm },
                recordCountThisWeek = records.size,
                progress = records.size.toFloat()
            )
        }
    }
}
