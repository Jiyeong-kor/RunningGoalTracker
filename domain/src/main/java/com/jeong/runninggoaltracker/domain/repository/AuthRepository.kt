package com.jeong.runninggoaltracker.domain.repository

import com.jeong.runninggoaltracker.domain.model.AuthResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun signInAnonymously(): Result<Unit>
    suspend fun reserveNicknameAndCreateUserProfile(nickname: String): AuthResult<Unit>
    suspend fun checkNicknameAvailability(nickname: String): AuthResult<Boolean>
    suspend fun deleteAccountAndReleaseNickname(): AuthResult<Unit>
    suspend fun upgradeAnonymousWithCustomToken(customToken: String): AuthResult<Unit>
    fun observeIsAnonymous(): Flow<Boolean>
}
