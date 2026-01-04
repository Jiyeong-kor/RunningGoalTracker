package com.jeong.runninggoaltracker.app.ui.navigation

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jeong.runninggoaltracker.app.di.MainTabItemsModule
import com.jeong.runninggoaltracker.shared.navigation.MainTab
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@UninstallModules(MainTabItemsModule::class)
class MainTabItemsProviderInjectionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var mainTabItemsProvider: MainTabItemsProvider

    @Test
    fun injectsFakeMainTabItemsProvider() {
        hiltRule.inject()

        val viewModel = MainNavigationViewModel(mainTabItemsProvider)
        val tabItems = viewModel.tabItemsByTab

        assertEquals(setOf(MainTab.RECORD), tabItems.keys)
    }
}
