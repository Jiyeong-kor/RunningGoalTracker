package com.jeong.runninggoaltracker.app.ui.navigation

import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MainScreenTest {

    @Test
    fun fromRouteReturnsMatchingScreen() {
        assertEquals(MainScreen.Home, MainScreen.fromRoute(MainNavigationRoute.Home))
        assertEquals(MainScreen.Record, MainScreen.fromRoute(MainNavigationRoute.Record))
        assertEquals(MainScreen.AiCoach, MainScreen.fromRoute(MainNavigationRoute.AiCoach))
        assertEquals(MainScreen.Goal, MainScreen.fromRoute(MainNavigationRoute.Goal))
        assertEquals(MainScreen.Reminder, MainScreen.fromRoute(MainNavigationRoute.Reminder))
        assertEquals(MainScreen.MyPage, MainScreen.fromRoute(MainNavigationRoute.MyPage))
    }

    @Test
    fun fromDestinationReturnsNullWhenMissing() {
        assertNull(MainScreen.fromDestination(null))
    }
}
