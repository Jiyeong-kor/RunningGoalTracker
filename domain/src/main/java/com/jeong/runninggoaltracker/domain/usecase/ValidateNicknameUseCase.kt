package com.jeong.runninggoaltracker.domain.usecase

import javax.inject.Inject

sealed interface NicknameValidationResult {
    data class Valid(val nickname: String) : NicknameValidationResult

    enum class Error : NicknameValidationResult {
        EMPTY,
        INVALID_FORMAT
    }
}

class ValidateNicknameUseCase @Inject constructor() {
    private val nicknameRegex = Regex("^[A-Za-z0-9가-힣]{2,10}$")

    operator fun invoke(input: String): NicknameValidationResult {
        if (input.isBlank()) {
            return NicknameValidationResult.Error.EMPTY
        }

        return if (nicknameRegex.matches(input)) {
            NicknameValidationResult.Valid(input)
        } else {
            NicknameValidationResult.Error.INVALID_FORMAT
        }
    }
}
