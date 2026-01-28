package com.jeong.runninggoaltracker.feature.record.presentation

import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionController
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.feature.record.api.RunningTrackerController
import com.jeong.runninggoaltracker.feature.record.api.RunningTrackerMonitor
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityLogEntry
import com.jeong.runninggoaltracker.feature.record.api.model.ActivityState
import com.jeong.runninggoaltracker.feature.record.api.model.RunningTrackerState
import com.jeong.runninggoaltracker.feature.record.viewmodel.RecordViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<RunningRecordRepository>()
    private val activityMonitor = mockk<ActivityRecognitionMonitor>()
    private val activityController = mockk<ActivityRecognitionController>(relaxed = true)
    private val runningTrackerMonitor = mockk<RunningTrackerMonitor>()
    private val runningTrackerController = mockk<RunningTrackerController>(relaxed = true)
    private val records = MutableStateFlow<List<RunningRecord>>(emptyList())
    private val activityState = MutableStateFlow(ActivityState())
    private val activityLogs = MutableStateFlow<List<ActivityLogEntry>>(emptyList())
    private val trackerState = MutableStateFlow(RunningTrackerState())

    private lateinit var viewModel: RecordViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { repository.getAllRecords() } returns records
        every { activityMonitor.activityState } returns activityState
        every { activityMonitor.activityLogs } returns activityLogs
        every { runningTrackerMonitor.trackerState } returns trackerState
        viewModel = RecordViewModel(
            getRunningRecordsUseCase = GetRunningRecordsUseCase(repository),
            activityRecognitionController = activityController,
            activityRecognitionMonitor = activityMonitor,
            runningTrackerController = runningTrackerController,
            runningTrackerMonitor = runningTrackerMonitor
        )
    }

    @After
    fun tearDown() =
        Dispatchers.resetMain()

    @Test
    fun `활동 인식 제어 요청을 위임한다`() = runTest {
        viewModel.startActivityRecognition()
        viewModel.stopActivityRecognition()

        verify { activityController.startUpdates() }
        verify { activityController.stopUpdates() }
    }

    @Test
    fun `트래커 상태를 UI로 노출한다`() = runTest {
        val trackerState = RunningTrackerState(
            isTracking = true,
            distanceKm = 1.5,
            elapsedMillis = 120_000,
            permissionRequired = false
        )
        this@RecordViewModelTest.trackerState.value = trackerState
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertTrue(uiState.isTracking)
        assertEquals(1.5, uiState.distanceKm, 0.0)
        assertEquals(120_000, uiState.elapsedMillis)
        assertEquals(false, uiState.permissionRequired)
    }

    @Test
    fun `러닝 트래킹 제어 요청을 위임한다`() = runTest {
        viewModel.startTracking()
        viewModel.stopTracking()

        verify { runningTrackerController.startTracking() }
        verify { runningTrackerController.stopTracking() }
    }
}
