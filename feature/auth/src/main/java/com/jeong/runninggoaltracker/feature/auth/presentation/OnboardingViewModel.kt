package com.jeong.runninggoaltracker.feature.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeong.runninggoaltracker.domain.usecase.SignInAnonymouslyUseCase
import com.jeong.runninggoaltracker.domain.usecase.UpdateNicknameUseCase
import com.jeong.runninggoaltracker.domain.usecase.NicknameValidationResult
import com.jeong.runninggoaltracker.domain.usecase.ValidateNicknameUseCase
import com.jeong.runninggoaltracker.feature.auth.domain.CheckInternetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.jeong.runninggoaltracker.feature.auth.R

enum class OnboardingStep {
    Permissions,
    Nickname,
    Success
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.Permissions,
    val nickname: String = "",
    val isLoading: Boolean = false,
    val isNicknameValid: Boolean = false,
    val nicknameValidationMessage: Int? = null,
    val errorMessageResId: Int? = null,
    val permissionErrorResId: Int? = null,
    val showNoInternetDialog: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val signInAnonymouslyUseCase: SignInAnonymouslyUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    private val validateNicknameUseCase: ValidateNicknameUseCase,
    private val checkInternetUseCase: CheckInternetUseCase
) : ViewModel() {
    var uiState by mutableStateOf(OnboardingUiState())
        private set

    fun onPermissionsResult(granted: Boolean) {
        uiState = if (granted) {
            uiState.copy(step = OnboardingStep.Nickname, permissionErrorResId = null)
        } else {
            uiState.copy(
                permissionErrorResId = R.string.permission_required_error
            )
        }
    }

    fun onNicknameChanged(value: String) {
        val trimmed = value.take(MAX_NICKNAME_LENGTH)
        val validationResult = validateNicknameUseCase(trimmed)
        val validationUi = validationResult.toUiState()
        uiState = uiState.copy(
            nickname = trimmed,
            isNicknameValid = validationUi.isValid,
            nicknameValidationMessage = validationUi.messageResId,
            errorMessageResId = null
        )
    }

    fun onContinueWithNickname() {
        val nickname = uiState.nickname
        val validationResult = validateNicknameUseCase(nickname)
        val validationUi = validationResult.toUiState()
        if (!validationUi.isValid) {
            uiState = uiState.copy(
                isNicknameValid = false,
                nicknameValidationMessage = validationUi.messageResId
            )
            return
        }
        if (!checkInternetUseCase()) {
            uiState = uiState.copy(showNoInternetDialog = true)
            return
        }
        viewModelScope.launch {
            uiState = uiState.copy(
                isLoading = true,
                errorMessageResId = null,
                nicknameValidationMessage = null,
                showNoInternetDialog = false
            )
            val signInResult = signInAnonymouslyUseCase()
            if (signInResult.isFailure) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessageResId = R.string.anonymous_sign_in_error
                )
                return@launch
            }
            val nicknameResult = updateNicknameUseCase(nickname)
            uiState = if (nicknameResult.isSuccess) {
                uiState.copy(isLoading = false, step = OnboardingStep.Success)
            } else {
                uiState.copy(
                    isLoading = false,
                    errorMessageResId = R.string.nickname_save_error
                )
            }
        }
    }

    fun onRetryInternet() {
        if (!checkInternetUseCase()) {
            uiState = uiState.copy(showNoInternetDialog = true)
            return
        }
        uiState = uiState.copy(showNoInternetDialog = false)
        onContinueWithNickname()
    }

    fun onDismissNoInternetDialog() {
        uiState = uiState.copy(showNoInternetDialog = false)
    }

    companion object {
        private const val MAX_NICKNAME_LENGTH = 10
    }

    private fun NicknameValidationResult.toUiState(): NicknameValidationUiState =
        when (this) {
            is NicknameValidationResult.Valid -> NicknameValidationUiState(
                isValid = true,
                messageResId = null
            )

            NicknameValidationResult.Error.EMPTY -> NicknameValidationUiState(
                isValid = false,
                messageResId = R.string.nickname_required_error
            )

            NicknameValidationResult.Error.INVALID_FORMAT -> NicknameValidationUiState(
                isValid = false,
                messageResId = R.string.nickname_invalid_error
            )
        }
}

private data class NicknameValidationUiState(
    val isValid: Boolean,
    val messageResId: Int?
)
