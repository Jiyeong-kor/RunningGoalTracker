package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.AuthError
import com.jeong.runninggoaltracker.domain.model.AuthResult
import com.jeong.runninggoaltracker.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthUseCaseContractTest {

    @Test
    fun `nickname reservation returns nickname taken for race condition`() = runTest {
        val repository = FakeAuthRepository(
            reserveResult = AuthResult.Failure(AuthError.NicknameTaken)
        )
        val useCase = ReserveNicknameAndCreateUserProfileUseCase(repository)

        val result = useCase("러너")

        assertEquals(AuthError.NicknameTaken, (result as AuthResult.Failure).error)
    }

    @Test
    fun `delete account returns permission denied when backend rejects`() = runTest {
        val repository = FakeAuthRepository(
            deleteResult = AuthResult.Failure(AuthError.PermissionDenied)
        )
        val useCase = DeleteAccountUseCase(repository)

        val result = useCase()

        assertEquals(AuthError.PermissionDenied, (result as AuthResult.Failure).error)
    }
}

private class FakeAuthRepository(
    private val reserveResult: AuthResult<Unit> = AuthResult.Success(Unit),
    private val checkResult: AuthResult<Boolean> = AuthResult.Success(true),
    private val deleteResult: AuthResult<Unit> = AuthResult.Success(Unit),
    private val upgradeResult: AuthResult<Unit> = AuthResult.Success(Unit)
) : AuthRepository {
    override suspend fun signInAnonymously(): Result<Unit> = Result.success(Unit)

    override suspend fun signInWithKakao(): Result<String> = Result.success("token")

    override suspend fun reserveNicknameAndCreateUserProfile(nickname: String): AuthResult<Unit> =
        reserveResult

    override suspend fun checkNicknameAvailability(nickname: String): AuthResult<Boolean> =
        checkResult

    override suspend fun deleteAccountAndReleaseNickname(): AuthResult<Unit> = deleteResult

    override suspend fun upgradeAnonymousWithCustomToken(customToken: String): AuthResult<Unit> =
        upgradeResult

    override fun observeIsAnonymous(): Flow<Boolean> = flowOf(false)

    override fun observeUserNickname(): Flow<String?> = flowOf(null)
}
