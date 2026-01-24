package com.jeong.runninggoaltracker.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateWeeklyGoalUseCaseTest {

    private val useCase = ValidateWeeklyGoalUseCase()

    @Test
    fun `입력값이 없으면 INVALID_NUMBER 반환`() {
        val result = useCase(null)

        assertEquals(WeeklyGoalValidationResult.Error.INVALID_NUMBER, result)
    }

    @Test
    fun `음수면 NON_POSITIVE 반환`() {
        val result = useCase(-3.0)

        assertEquals(WeeklyGoalValidationResult.Error.NON_POSITIVE, result)
    }

    @Test
    fun `양수면 Valid 반환`() {
        val result = useCase(8.5)

        assertTrue(result is WeeklyGoalValidationResult.Valid)
        val valid = result as WeeklyGoalValidationResult.Valid
        assertEquals(8.5, valid.weeklyGoalKm, 0.0)
    }
}
