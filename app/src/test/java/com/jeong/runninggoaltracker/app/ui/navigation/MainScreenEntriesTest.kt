package com.jeong.runninggoaltracker.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class MainScreenEntriesTest {

    @Test
    fun entriesContainAllScreensInOrder() {
        val expected = listOf(
            MainScreen.Home,
            MainScreen.Record,
            MainScreen.AiCoach,
            MainScreen.Goal,
            MainScreen.Reminder,
            MainScreen.MyPage
        )

        assertEquals(expected, MainScreen.entries)
    }
}
