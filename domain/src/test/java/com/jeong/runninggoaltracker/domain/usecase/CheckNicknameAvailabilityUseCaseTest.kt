package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.AuthError
import com.jeong.runninggoaltracker.domain.model.AuthResult
import com.jeong.runninggoaltracker.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckNicknameAvailabilityUseCaseTest {

    @Test
    fun `닉네임 요청을 리포지토리에 전달`() = runBlocking {
        val repository = FakeAuthRepository(
            result = AuthResult.Success(true)
        )
        val useCase = CheckNicknameAvailabilityUseCase(repository)

        val result = useCase("runner")

        assertEquals("runner", repository.requestedNickname)
        assertEquals(AuthResult.Success(true), result)
    }

    @Test
    fun `오류 결과를 그대로 반환`() = runBlocking {
        val failure = AuthResult.Failure(AuthError.NicknameTaken)
        val repository = FakeAuthRepository(
            result = failure
        )
        val useCase = CheckNicknameAvailabilityUseCase(repository)

        val result = useCase("taken")

        assertEquals("taken", repository.requestedNickname)
        assertEquals(failure, result)
    }

    private class FakeAuthRepository(
        private val result: AuthResult<Boolean>
    ) : AuthRepository {
        var requestedNickname: String? = null

        override suspend fun signInAnonymously(): Result<Unit> = error("Not used")

        override suspend fun signInWithKakao(): Result<String> = error("Not used")

        override suspend fun reserveNicknameAndCreateUserProfile(nickname: String): AuthResult<Unit> =
            error("Not used")

        override suspend fun checkNicknameAvailability(nickname: String): AuthResult<Boolean> {
            requestedNickname = nickname
            return result
        }

        override suspend fun deleteAccountAndReleaseNickname(): AuthResult<Unit> = error("Not used")

        override suspend fun upgradeAnonymousWithCustomToken(customToken: String): AuthResult<Unit> =
            error("Not used")

        override fun observeIsAnonymous(): Flow<Boolean> = emptyFlow()

        override fun observeUserNickname(): Flow<String?> = emptyFlow()
    }
}
