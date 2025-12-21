package com.jeong.runninggoaltracker.feature.home.presentation

import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningGoalRepository
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import com.jeong.runninggoaltracker.domain.usecase.GetRunningSummaryUseCase
import com.jeong.runninggoaltracker.domain.usecase.WeeklySummaryCalculator
import com.jeong.runninggoaltracker.domain.util.DateProvider
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val goalRepository = FakeRunningGoalRepository()
    private val recordRepository = FakeRunningRecordRepository()
    private val today = LocalDate.of(2024, 6, 7)
    private val dateProvider = FakeDateProvider(today)

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val useCase = GetRunningSummaryUseCase(
            goalRepository = goalRepository,
            recordRepository = recordRepository,
            dateProvider = dateProvider,
            summaryCalculator = WeeklySummaryCalculator()
        )
        viewModel = HomeViewModel(useCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `주간 목표와 기록이 주어지면 요약 정보를 계산해 노출한다`() = runTest {
        val mondayOfWeek = today.with(java.time.DayOfWeek.MONDAY)
        val thisWeekRecords = listOf(
            RunningRecord(date = mondayOfWeek, distanceKm = 5.0, durationMinutes = 30),
            RunningRecord(date = today, distanceKm = 3.5, durationMinutes = 20)
        )
        val lastWeekRecord = RunningRecord(
            date = today.minusDays(7),
            distanceKm = 10.0,
            durationMinutes = 60
        )

        goalRepository.upsertGoal(RunningGoal(weeklyGoalKm = 12.0))
        recordRepository.setRecords(thisWeekRecords + lastWeekRecord)

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(12.0, state.weeklyGoalKm ?: 0.0, 0.0)
        assertEquals(8.5, state.totalThisWeekKm, 0.0)
        assertEquals(2, state.recordCountThisWeek)
        assertEquals(0.7083333f, state.progress, 0.0001f)
    }

    @Test
    fun `목표가 없으면 기본 상태를 유지한다`() = runTest {
        recordRepository.setRecords(emptyList())

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(null, state.weeklyGoalKm)
        assertEquals(0.0, state.totalThisWeekKm, 0.0)
        assertEquals(0, state.recordCountThisWeek)
        assertEquals(0f, state.progress)
    }

    private class FakeRunningGoalRepository : RunningGoalRepository {
        private val goalState = MutableStateFlow<RunningGoal?>(null)

        override fun getGoal(): Flow<RunningGoal?> = goalState

        override suspend fun upsertGoal(goal: RunningGoal) {
            goalState.value = goal
        }
    }

    private class FakeRunningRecordRepository : RunningRecordRepository {
        private val recordsState = MutableStateFlow<List<RunningRecord>>(emptyList())

        override fun getAllRecords(): Flow<List<RunningRecord>> = recordsState

        override suspend fun addRecord(record: RunningRecord) {
            recordsState.value = recordsState.value + record
        }

        fun setRecords(records: List<RunningRecord>) {
            recordsState.value = records
        }
    }

    private class FakeDateProvider(private val today: LocalDate) : DateProvider {
        private val todayFlow = MutableStateFlow(today)

        override fun getTodayFlow(): Flow<LocalDate> = todayFlow
        override fun getToday(): LocalDate = today
    }
}
