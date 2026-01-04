package com.jeong.runninggoaltracker.app.di

import com.jeong.runninggoaltracker.app.ui.navigation.DefaultMainTabItemsProvider
import com.jeong.runninggoaltracker.app.ui.navigation.MainTabItemsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MainTabItemsModule {
    @Binds
    abstract fun bindMainTabItemsProvider(
        impl: DefaultMainTabItemsProvider
    ): MainTabItemsProvider
}
