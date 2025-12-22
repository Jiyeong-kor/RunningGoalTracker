package com.jeong.runninggoaltracker.data.di

import android.content.Context
import com.jeong.runninggoaltracker.data.local.RunningDatabase
import com.jeong.runninggoaltracker.data.local.RunningDatabaseFactory
import com.jeong.runninggoaltracker.data.local.RunningGoalDao
import com.jeong.runninggoaltracker.data.local.RunningRecordDao
import com.jeong.runninggoaltracker.data.local.RunningReminderDao
import com.jeong.runninggoaltracker.data.repository.RunningGoalRepositoryImpl
import com.jeong.runninggoaltracker.data.repository.RunningRecordRepositoryImpl
import com.jeong.runninggoaltracker.data.repository.RunningReminderRepositoryImpl
import com.jeong.runninggoaltracker.data.util.SystemDateProvider
import com.jeong.runninggoaltracker.domain.repository.RunningGoalRepository
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import com.jeong.runninggoaltracker.domain.repository.RunningReminderRepository
import com.jeong.runninggoaltracker.domain.util.DateProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataProvidesModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(
        @ApplicationContext context: Context,
        factory: RunningDatabaseFactory,
    ): RunningDatabase = factory.create(context)

    @Provides
    fun provideRunningRecordDao(db: RunningDatabase): RunningRecordDao = db.runningRecordDao()

    @Provides
    fun provideRunningGoalDao(db: RunningDatabase): RunningGoalDao = db.runningGoalDao()

    @Provides
    fun provideRunningReminderDao(db: RunningDatabase): RunningReminderDao = db.runningReminderDao()

    @Provides
    @Singleton
    fun provideDateProvider(@ApplicationContext context: Context): DateProvider =
        SystemDateProvider(context)
}

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindsModule {

    @Binds
    @Singleton
    abstract fun bindRunningGoalRepository(impl: RunningGoalRepositoryImpl): RunningGoalRepository

    @Binds
    @Singleton
    abstract fun bindRunningRecordRepository(impl: RunningRecordRepositoryImpl): RunningRecordRepository

    @Binds
    @Singleton
    abstract fun bindRunningReminderRepository(impl: RunningReminderRepositoryImpl): RunningReminderRepository
}
