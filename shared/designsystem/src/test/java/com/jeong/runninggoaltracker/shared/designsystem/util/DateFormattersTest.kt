package com.jeong.runninggoaltracker.shared.designsystem.util

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DateFormattersTest {

    private lateinit var defaultLocale: Locale

    @Before
    fun setUp() {
        defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.KOREA)
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun `소수점 없이 전체 킬로미터를 표시`() {
        assertEquals("5 km", 5.0.toDistanceLabel())
    }

    @Test
    fun `소수점 한 자리까지의 킬로미터를 표시`() {
        assertEquals("5.3 km", 5.34.toDistanceLabel())
    }
}
