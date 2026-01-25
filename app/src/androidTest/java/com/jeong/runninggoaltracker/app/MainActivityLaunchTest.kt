package com.jeong.runninggoaltracker.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MainActivityLaunchTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun launchDisplaysRootView() {
        hiltRule.inject()

        ActivityScenario.launch(MainActivity::class.java).use {
            onView(isRoot()).check(matches(isDisplayed()))
        }
    }
}
