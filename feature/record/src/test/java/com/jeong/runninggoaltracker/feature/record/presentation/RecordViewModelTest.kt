package com.jeong.runninggoaltracker.feature.record.presentation

import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import com.jeong.runninggoaltracker.domain.usecase.AddRunningRecordUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.domain.usecase.ValidateRunningRecordInputUseCase
import com.jeong.runninggoaltracker.domain.util.DateProvider
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionController
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityState
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = FakeRunningRecordRepository()
    private val dateProvider = FakeDateProvider(LocalDate.of(2024, 6, 10))
    private val activityMonitor = FakeActivityRecognitionMonitor()
    private val activityController = FakeActivityRecognitionController()

    private lateinit var viewModel: RecordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RecordViewModel(
            getRunningRecordsUseCase = GetRunningRecordsUseCase(repository),
            addRunningRecordUseCase = AddRunningRecordUseCase(repository),
            dateProvider = dateProvider,
            validateRunningRecordInputUseCase = ValidateRunningRecordInputUseCase(),
            activityRecognitionController = activityController,
            activityRecognitionMonitor = activityMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `잘못된 숫자 입력 시 오류 상태를 노출한다`() = runTest {
        viewModel.onDistanceChanged("abc")
        viewModel.onDurationChanged("10")

        viewModel.saveRecord()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(RecordInputError.INVALID_NUMBER, viewModel.uiState.value.error)
        assertTrue(repository.records.value.isEmpty())
    }

    @Test
    fun `0 이하의 값을 입력하면 NON_POSITIVE 오류를 반환한다`() = runTest {
        viewModel.onDistanceChanged("0")
        viewModel.onDurationChanged("-5")

        viewModel.saveRecord()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(RecordInputError.NON_POSITIVE, viewModel.uiState.value.error)
        assertTrue(repository.records.value.isEmpty())
    }

    @Test
    fun `유효한 입력이면 기록을 저장하고 입력 값을 초기화한다`() = runTest {
        viewModel.onDistanceChanged("6.5")
        viewModel.onDurationChanged("42")

        viewModel.saveRecord()
        testDispatcher.scheduler.advanceUntilIdle()

        val saved = repository.records.value.single()
        assertEquals(dateProvider.getToday(), saved.date)
        assertEquals(6.5, saved.distanceKm, 0.0)
        assertEquals(42, saved.durationMinutes)

        val currentState = viewModel.uiState.value
        assertEquals("", currentState.distanceInput)
        assertEquals("", currentState.durationInput)
        assertEquals(null, currentState.error)
    }

    @Test
    fun `활동 인식 제어 요청을 위임한다`() = runTest {
        viewModel.startActivityRecognition { activityController.permissionRequested = true }
        viewModel.stopActivityRecognition()
        viewModel.notifyPermissionDenied()

        assertTrue(activityController.started)
        assertTrue(activityController.stopped)
        assertTrue(activityController.permissionRequested)
    }

    private class FakeRunningRecordRepository : RunningRecordRepository {
        val records = MutableStateFlow<List<RunningRecord>>(emptyList())

        override fun getAllRecords(): Flow<List<RunningRecord>> = records

        override suspend fun addRecord(record: RunningRecord) {
            records.value += record
        }
    }

    private class FakeDateProvider(
        private val _today: LocalDate
    ) : DateProvider {
        private val todayFlow = MutableStateFlow(_today)
        override fun getTodayFlow(): Flow<LocalDate> = todayFlow
        override fun getToday(): LocalDate = _today
    }

    private class FakeActivityRecognitionController : ActivityRecognitionController {
        var started = false
        var stopped = false
        var permissionRequested = false

        override fun startUpdates(onPermissionRequired: () -> Unit) {
            started = true
            onPermissionRequired()
        }

        override fun stopUpdates() {
            stopped = true
        }

        override fun notifyPermissionDenied() {
            permissionRequested = true
        }
    }

    private class FakeActivityRecognitionMonitor : ActivityRecognitionMonitor {
        private val _activityState = MutableStateFlow(ActivityState())
        override val activityState: StateFlow<ActivityState> = _activityState
        override val activityLogs: StateFlow<List<ActivityLogEntry>> =
            MutableStateFlow(emptyList())
    }
}
