package com.jeong.runninggoaltracker.feature.record.presentation

import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.domain.util.DateFormatter
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionController
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.feature.record.api.RunningTrackerController
import com.jeong.runninggoaltracker.feature.record.api.RunningTrackerMonitor
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityState
import com.jeong.runninggoaltracker.feature.record.tracking.RunningTrackerState
import com.jeong.runninggoaltracker.feature.record.viewmodel.RecordViewModel
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
    private val activityMonitor = FakeActivityRecognitionMonitor()
    private val activityController = FakeActivityRecognitionController()
    private val runningTrackerMonitor = FakeRunningTrackerMonitor()
    private val runningTrackerController = FakeRunningTrackerController()

    private lateinit var viewModel: RecordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RecordViewModel(
            dateFormatter = FakeDateFormatter(),
            getRunningRecordsUseCase = GetRunningRecordsUseCase(repository),
            activityRecognitionController = activityController,
            activityRecognitionMonitor = activityMonitor,
            runningTrackerController = runningTrackerController,
            runningTrackerMonitor = runningTrackerMonitor
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
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

    @Test
    fun `트래커 상태를 UI로 노출한다`() = runTest {
        val trackerState = RunningTrackerState(
            isTracking = true,
            distanceKm = 1.5,
            elapsedMillis = 120_000,
            permissionRequired = false
        )
        runningTrackerMonitor.update(trackerState)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isTracking)
        assertEquals(1.5, uiState.distanceKm, 0.0)
        assertEquals(120_000, uiState.elapsedMillis)
        assertEquals(false, uiState.permissionRequired)
    }

    @Test
    fun `러닝 트래킹 제어 요청을 위임한다`() = runTest {
        viewModel.startTracking { runningTrackerController.permissionRequested = true }
        viewModel.stopTracking()
        viewModel.notifyTrackingPermissionDenied()

        assertTrue(runningTrackerController.started)
        assertTrue(runningTrackerController.stopped)
        assertTrue(runningTrackerController.permissionRequested)
    }

    private class FakeRunningRecordRepository : RunningRecordRepository {
        val records = MutableStateFlow<List<RunningRecord>>(emptyList())

        override fun getAllRecords(): Flow<List<RunningRecord>> = records

        override suspend fun addRecord(record: RunningRecord) {
            records.value += record
        }
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

    private class FakeRunningTrackerMonitor : RunningTrackerMonitor {
        private val _state = MutableStateFlow(RunningTrackerState())
        override val trackerState: StateFlow<RunningTrackerState> = _state

        fun update(state: RunningTrackerState) {
            _state.value = state
        }
    }

    private class FakeRunningTrackerController : RunningTrackerController {
        var started = false
        var stopped = false
        var permissionRequested = false

        override fun startTracking(onPermissionRequired: () -> Unit) {
            started = true
            onPermissionRequired()
        }

        override fun stopTracking() {
            stopped = true
        }

        override fun notifyPermissionDenied() {
            permissionRequested = true
        }
    }

    private class FakeDateFormatter : DateFormatter {
        override fun formatToKoreanDate(timestamp: Long): String = timestamp.toString()

        override fun formatToDistanceLabel(distanceKm: Double): String =
            "${distanceKm}km"

        override fun formatElapsedTime(elapsedMillis: Long): String = elapsedMillis.toString()
    }
}
