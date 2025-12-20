package com.jeong.runninggoaltracker.feature.record.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.usecase.AddRunningRecordUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.domain.usecase.RunningRecordValidationResult
import com.jeong.runninggoaltracker.domain.usecase.ValidateRunningRecordInputUseCase
import com.jeong.runninggoaltracker.domain.util.DateProvider
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionController
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordUiState(
    val records: List<RunningRecord> = emptyList(),
    val distanceInput: String = "",
    val durationInput: String = "",
    val error: RecordInputError? = null,
    val activityLabel: String = "UNKNOWN"
)

enum class RecordInputError {
    INVALID_NUMBER,
    NON_POSITIVE
}

private data class RecordInputState(
    val distanceInput: String = "",
    val durationInput: String = "",
    val error: RecordInputError? = null
)

@HiltViewModel
class RecordViewModel @Inject constructor(
    getRunningRecordsUseCase: GetRunningRecordsUseCase,
    private val addRunningRecordUseCase: AddRunningRecordUseCase,
    private val dateProvider: DateProvider,
    private val validateRunningRecordInputUseCase: ValidateRunningRecordInputUseCase,
    private val activityRecognitionController: ActivityRecognitionController,
    activityRecognitionMonitor: ActivityRecognitionMonitor
) : ViewModel() {

    private val inputState = MutableStateFlow(RecordInputState())

    val uiState: StateFlow<RecordUiState> = combine(
        getRunningRecordsUseCase(),
        inputState,
        activityRecognitionMonitor.activityState
    ) { records, input, activity ->
        RecordUiState(
            records = records,
            distanceInput = input.distanceInput,
            durationInput = input.durationInput,
            error = input.error,
            activityLabel = activity.label
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RecordUiState()
    )

    fun onDistanceChanged(value: String) {
        inputState.update { current ->
            current.copy(distanceInput = value, error = null)
        }
    }

    fun onDurationChanged(value: String) {
        inputState.update { current ->
            current.copy(durationInput = value, error = null)
        }
    }

    fun saveRecord() {
        when (val result = validateRunningRecordInputUseCase(
            inputState.value.distanceInput,
            inputState.value.durationInput
        )) {
            RunningRecordValidationResult.Error.INVALID_NUMBER -> {
                inputState.update { current ->
                    current.copy(error = RecordInputError.INVALID_NUMBER)
                }
            }

            RunningRecordValidationResult.Error.NON_POSITIVE -> {
                inputState.update { current ->
                    current.copy(error = RecordInputError.NON_POSITIVE)
                }
            }

            is RunningRecordValidationResult.Valid -> {
                viewModelScope.launch {
                    addRunningRecordUseCase(
                        date = dateProvider.getToday(),
                        distanceKm = result.distanceKm,
                        durationMinutes = result.durationMinutes
                    )
                    inputState.value = RecordInputState()
                }
            }
        }
    }

    fun startActivityRecognition(onPermissionRequired: () -> Unit) {
        activityRecognitionController.startUpdates(onPermissionRequired)
    }

    fun stopActivityRecognition() {
        activityRecognitionController.stopUpdates()
    }

    fun notifyPermissionDenied() {
        activityRecognitionController.notifyPermissionDenied()
    }
}
