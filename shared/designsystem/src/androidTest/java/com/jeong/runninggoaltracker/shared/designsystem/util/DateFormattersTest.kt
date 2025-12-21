package com.jeong.runninggoaltracker.shared.designsystem.util

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DateFormattersTest {

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun formats_local_date_to_korean_label() {
        val date = LocalDate.of(2024, 11, 8)

        val formatted = date.toKoreanDateLabel()

        assertEquals("11월 8일 (금)", formatted)
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun formats_iso_date_string_safely() {
        val formatted = "2024-12-01".toKoreanDateLabel()
        val fallback = "invalid-date".toKoreanDateLabel()

        assertEquals("12월 1일 (일)", formatted)
        assertEquals("invalid-date", fallback)
    }

    @Test
    fun formats_distance_label_with_minimal_precision() {
        assertEquals("12 km", 12.0.toDistanceLabel())
        assertEquals("3.5 km", 3.456.toDistanceLabel())
    }
}
