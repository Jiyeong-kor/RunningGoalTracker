package com.jeong.runninggoaltracker.data.local

import java.time.DayOfWeek
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RunningConvertersTest {

    private val dayOfWeekConverter = DayOfWeekConverter()
    private val localDateConverter = LocalDateConverter()

    @Test
    fun `LocalDate를 문자열로 변환하고 다시 복원`() {
        val date = LocalDate.of(2024, 5, 12)

        val timestamp = localDateConverter.dateToTimestamp(date)

        assertEquals("2024-05-12", timestamp)
        assertEquals(date, localDateConverter.fromTimestamp(timestamp))
    }

    @Test
    fun `널인 LocalDate를 변환할 때 안전하게 처리`() {
        assertNull(localDateConverter.dateToTimestamp(null))
        assertNull(localDateConverter.fromTimestamp(null))
    }

    @Test
    fun `요일 집합을 직렬화하고 다시 역직렬화`() {
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)

        val serialized = dayOfWeekConverter.toDays(days)
        val deserialized = dayOfWeekConverter.fromDays(serialized)

        assertEquals(setOf("1", "5"), serialized.split(",").toSet())
        assertEquals(days, deserialized)
    }

    @Test
    fun `잘못된 문자열은 무시하고 유효한 요일만 반환`() {
        val deserialized = dayOfWeekConverter.fromDays("abc,,1,10")

        assertEquals(setOf(DayOfWeek.MONDAY), deserialized)
    }
}
