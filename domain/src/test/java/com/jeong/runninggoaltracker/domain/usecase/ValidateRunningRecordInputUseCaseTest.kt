package com.jeong.runninggoaltracker.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidateRunningRecordInputUseCaseTest {

    private lateinit var useCase: ValidateRunningRecordInputUseCase

    @Before
    fun setUp() {
        useCase = ValidateRunningRecordInputUseCase()
    }

    @Test
    fun `returns error when distance or duration is not a number`() {
        val distanceError = useCase(distanceInput = "five", durationInput = "30")
        val durationError = useCase(distanceInput = "5.0", durationInput = "thirty")

        assertEquals(RunningRecordValidationResult.Error.INVALID_NUMBER, distanceError)
        assertEquals(RunningRecordValidationResult.Error.INVALID_NUMBER, durationError)
    }

    @Test
    fun `returns error when distance or duration is non-positive`() {
        val zeroDistance = useCase(distanceInput = "0", durationInput = "30")
        val negativeDuration = useCase(distanceInput = "5.0", durationInput = "-1")

        assertEquals(RunningRecordValidationResult.Error.NON_POSITIVE, zeroDistance)
        assertEquals(RunningRecordValidationResult.Error.NON_POSITIVE, negativeDuration)
    }

    @Test
    fun `returns valid result when both inputs are positive numbers`() {
        val result = useCase(distanceInput = "5.5", durationInput = "45")

        assertTrue(result is RunningRecordValidationResult.Valid)
        val validResult = result as RunningRecordValidationResult.Valid
        assertEquals(5.5, validResult.distanceKm, 0.0)
        assertEquals(45, validResult.durationMinutes)
    }
}
