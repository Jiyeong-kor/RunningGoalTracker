package com.jeong.runninggoaltracker.domain.usecase.squat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmaFilterTest {
    @Test
    fun firstUpdateSetsCurrentValue() {
        val filter = EmaFilter(alpha = 0.5f)

        assertNull(filter.current())

        val result = filter.update(10f)

        assertEquals(10f, result, 0.0001f)
        assertEquals(10f, filter.current() ?: 0f, 0.0001f)
    }

    @Test
    fun updateAppliesAlphaForSubsequentValues() {
        val filter = EmaFilter(alpha = 0.5f)

        filter.update(10f)
        val result = filter.update(14f)

        assertEquals(12f, result, 0.0001f)
        assertEquals(12f, filter.current() ?: 0f, 0.0001f)
    }
}
