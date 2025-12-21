package com.jeong.runninggoaltracker.feature.record.recognition

import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityRecognitionStateHolderTest {

    @Test
    fun `초기 상태는 UNKNOWN 레이블을 가진다`() {
        val holder = ActivityRecognitionStateHolder()

        assertEquals("UNKNOWN", holder.state.value.label)
    }

    @Test
    fun `업데이트 호출 시 현재 상태가 변경된다`() {
        val holder = ActivityRecognitionStateHolder()

        holder.update("RUNNING")

        assertEquals("RUNNING", holder.state.value.label)
    }
}
