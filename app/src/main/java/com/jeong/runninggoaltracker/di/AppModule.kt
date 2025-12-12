package com.jeong.runninggoaltracker.di

import android.content.Context
import com.jeong.runninggoaltracker.data.local.RunningDao
import com.jeong.runninggoaltracker.data.local.RunningDatabase
import com.jeong.runninggoaltracker.data.local.RunningDatabaseFactory
import com.jeong.runninggoaltracker.data.repository.RunningGoalRepositoryImpl
import com.jeong.runninggoaltracker.data.repository.RunningRecordRepositoryImpl
import com.jeong.runninggoaltracker.data.repository.RunningReminderRepositoryImpl
import com.jeong.runninggoaltracker.domain.repository.RunningGoalRepository
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import com.jeong.runninggoaltracker.domain.repository.RunningReminderRepository
import com.jeong.runninggoaltracker.domain.usecase.AddRunningRecordUseCase
import com.jeong.runninggoaltracker.domain.usecase.CreateDefaultReminderUseCase
import com.jeong.runninggoaltracker.domain.usecase.DeleteRunningReminderUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningGoalUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRecordsUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRemindersUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningSummaryUseCase
import com.jeong.runninggoaltracker.domain.usecase.RunningSummaryCalculator
import com.jeong.runninggoaltracker.domain.usecase.ToggleReminderDayUseCase
import com.jeong.runninggoaltracker.domain.usecase.UpsertRunningGoalUseCase
import com.jeong.runninggoaltracker.domain.usecase.UpsertRunningReminderUseCase
import com.jeong.runninggoaltracker.domain.usecase.WeeklySummaryCalculator
import com.jeong.runninggoaltracker.domain.util.DateProvider
import com.jeong.runninggoaltracker.util.SystemDateProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideRunningDatabase(
        @ApplicationContext context: Context,
        factory: RunningDatabaseFactory,
    ): RunningDatabase = factory.create(context)

    @Provides
    fun provideRunningDao(db: RunningDatabase): RunningDao = db.runningDao()

    @Provides
    @Singleton
    fun provideDateProvider(): DateProvider = SystemDateProvider()

    @Provides
    fun provideAddRunningRecordUseCase(repository: RunningRecordRepository): AddRunningRecordUseCase =
        AddRunningRecordUseCase(repository)

    @Provides
    fun provideGetRunningRecordsUseCase(
        repository: RunningRecordRepository
    ): GetRunningRecordsUseCase = GetRunningRecordsUseCase(repository)

    @Provides
    fun provideGetRunningSummaryUseCase(
        goalRepository: RunningGoalRepository,
        recordRepository: RunningRecordRepository,
        dateProvider: DateProvider,
        summaryCalculator: RunningSummaryCalculator
    ): GetRunningSummaryUseCase =
        GetRunningSummaryUseCase(goalRepository, recordRepository, dateProvider, summaryCalculator)

    @Provides
    fun provideRunningSummaryCalculator(): RunningSummaryCalculator =
        WeeklySummaryCalculator()

    @Provides
    fun provideDeleteRunningReminderUseCase(repository: RunningReminderRepository): DeleteRunningReminderUseCase =
        DeleteRunningReminderUseCase(repository)

    @Provides
    fun provideGetRunningGoalUseCase(repository: RunningGoalRepository): GetRunningGoalUseCase =
        GetRunningGoalUseCase(repository)

    @Provides
    @Singleton
    fun provideGetRunningRemindersUseCase(
        repository: RunningReminderRepository
    ): GetRunningRemindersUseCase = GetRunningRemindersUseCase(repository)

    @Provides
    @Singleton
    fun provideCreateDefaultReminderUseCase(): CreateDefaultReminderUseCase =
        CreateDefaultReminderUseCase()

    @Provides
    @Singleton
    fun provideToggleReminderDayUseCase(): ToggleReminderDayUseCase = ToggleReminderDayUseCase()

    @Provides
    fun provideUpsertRunningGoalUseCase(repository: RunningGoalRepository): UpsertRunningGoalUseCase =
        UpsertRunningGoalUseCase(repository)

    @Provides
    @Singleton
    fun provideUpsertRunningReminderUseCase(
        repository: RunningReminderRepository
    ): UpsertRunningReminderUseCase = UpsertRunningReminderUseCase(repository)
}

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindsModule {

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
