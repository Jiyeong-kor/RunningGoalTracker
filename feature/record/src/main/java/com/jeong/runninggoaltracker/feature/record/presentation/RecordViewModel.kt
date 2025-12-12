package com.jeong.runninggoaltracker.feature.record.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.usecase.AddRunningRecordUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.domain.util.DateProvider
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
    val error: RecordInputError? = null
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
    private val dateProvider: DateProvider
) : ViewModel() {

    private val inputState = MutableStateFlow(RecordInputState())

    val uiState: StateFlow<RecordUiState> = combine(
        getRunningRecordsUseCase(),
        inputState
    ) { records, input ->
        RecordUiState(
            records = records,
            distanceInput = input.distanceInput,
            durationInput = input.durationInput,
            error = input.error
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
        val distance = inputState.value.distanceInput.toDoubleOrNull()
        val duration = inputState.value.durationInput.toIntOrNull()

        when {
            distance == null || duration == null -> {
                inputState.update { current ->
                    current.copy(error = RecordInputError.INVALID_NUMBER)
                }
            }

            distance <= 0.0 || duration <= 0 -> {
                inputState.update { current ->
                    current.copy(error = RecordInputError.NON_POSITIVE)
                }
            }

            else -> {
                viewModelScope.launch {
                    addRunningRecordUseCase(
                        date = dateProvider.getToday(),
                        distanceKm = distance,
                        durationMinutes = duration
                    )
                    inputState.value = RecordInputState()
                }
            }
        }
    }
}
