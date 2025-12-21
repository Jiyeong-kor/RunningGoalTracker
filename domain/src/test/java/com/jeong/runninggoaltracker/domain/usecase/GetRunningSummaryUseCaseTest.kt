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
import org.junit.Test

class GetRunningSummaryUseCaseTest {

    @Test
    fun `최신 목표와 기록 및 오늘 날짜 기준으로 summary emit`() = runBlocking {
        val testSetup = createUseCase(today = LocalDate.of(2024, 12, 1))

        testSetup.goalRepository.upsertGoal(RunningGoal(weeklyGoalKm = 25.0))
        testSetup.recordRepository.addRecord(
            RunningRecord(
                date = LocalDate.of(2024, 11, 25),
                distanceKm = 6.0,
                durationMinutes = 30
            )
        )
        testSetup.recordRepository.addRecord(
            RunningRecord(
                date = LocalDate.of(2024, 11, 26),
                distanceKm = 4.0,
                durationMinutes = 25
            )
        )

        val summary = testSetup.useCase().first()

        assertEquals(25.0, summary.weeklyGoalKm!!, 0.0)
        assertEquals(10.0, summary.totalThisWeekKm, 0.0)
        assertEquals(2, summary.recordCountThisWeek)
        assertEquals(2f, summary.progress)
        assertEquals(
            listOf(
                FakeRunningSummaryCalculator.Invocation(
                    goal = RunningGoal(weeklyGoalKm = 25.0),
                    records = testSetup.recordRepository.state.value,
                    today = LocalDate.of(2024, 12, 1)
                )
            ),
            testSetup.summaryCalculator.invocations
        )
    }

    @Test
    fun `목표나 기록 변경 시 summary 업데이트`() = runBlocking {
        val testSetup = createUseCase(today = LocalDate.of(2024, 12, 1))
        val summaries = mutableListOf<RunningSummary>()

        val collectionJob = launch {
            testSetup.useCase().take(2).toList(summaries)
        }

        testSetup.goalRepository.upsertGoal(RunningGoal(weeklyGoalKm = 12.0))
        testSetup.recordRepository.addRecord(
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

    private fun createUseCase(today: LocalDate): TestSetup {
        val goalRepository = FakeRunningGoalRepository()
        val recordRepository = FakeRunningRecordRepository()
        val dateProvider = FakeDateProvider(today)
        val summaryCalculator = FakeRunningSummaryCalculator()

        return TestSetup(
            goalRepository = goalRepository,
            recordRepository = recordRepository,
            summaryCalculator = summaryCalculator,
            useCase = GetRunningSummaryUseCase(
                goalRepository = goalRepository,
                recordRepository = recordRepository,
                dateProvider = dateProvider,
                summaryCalculator = summaryCalculator
            )
        )
    }

    private data class TestSetup(
        val goalRepository: FakeRunningGoalRepository,
        val recordRepository: FakeRunningRecordRepository,
        val summaryCalculator: FakeRunningSummaryCalculator,
        val useCase: GetRunningSummaryUseCase
    )

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

    private class FakeDateProvider(private val today: LocalDate) : DateProvider {
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
