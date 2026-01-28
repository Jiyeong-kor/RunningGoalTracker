package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithKakaoUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): Result<String> = repository.signInWithKakao()
}
