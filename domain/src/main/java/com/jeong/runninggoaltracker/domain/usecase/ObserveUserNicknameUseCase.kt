package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserNicknameUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<String?> = repository.observeUserNickname()
}
