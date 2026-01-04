package com.jeong.runninggoaltracker.app.ui.navigation

import com.jeong.runninggoaltracker.app.di.MainTabItemsModule
import com.jeong.runninggoaltracker.shared.navigation.BottomTabIcon
import com.jeong.runninggoaltracker.shared.navigation.MainNavigationRoute
import com.jeong.runninggoaltracker.shared.navigation.MainTab
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [MainTabItemsModule::class]
)
object FakeMainTabItemsModule {
    @Provides
    fun provideFakeMainTabItemsProvider(): MainTabItemsProvider = FakeMainTabItemsProvider()
}

private class FakeMainTabItemsProvider : MainTabItemsProvider {
    override fun tabItemsByTab(): Map<MainTab, MainTabItem> = mapOf(
        MainTab.RECORD to MainTabItem(
            tab = MainTab.RECORD,
            titleResId = MainScreen.fromRoute(MainNavigationRoute.Record)!!.titleResId,
            icon = BottomTabIcon.RECORD.asImageVector()!!
        )
    )
}
