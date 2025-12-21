package com.jeong.runninggoaltracker.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateWeeklyGoalUseCaseTest {

    private lateinit var useCase: ValidateWeeklyGoalUseCase

    @Before
    fun setUp() {
        useCase = ValidateWeeklyGoalUseCase()
    }

    @Test
    fun `returns error when input is not a number`() {
        val result = useCase("ten")

        assertEquals(WeeklyGoalValidationResult.Error.INVALID_NUMBER, result)
    }

    @Test
    fun `returns error when input is zero or negative`() {
        val zeroResult = useCase("0")
        val negativeResult = useCase("-1.5")

        assertEquals(WeeklyGoalValidationResult.Error.NON_POSITIVE, zeroResult)
        assertEquals(WeeklyGoalValidationResult.Error.NON_POSITIVE, negativeResult)
    }

    @Test
    fun `returns valid result for positive numbers`() {
        val result = useCase("12.5")

        assertTrue(result is WeeklyGoalValidationResult.Valid)
        assertEquals(12.5, (result as WeeklyGoalValidationResult.Valid).weeklyGoalKm, 0.0)
    }
}
