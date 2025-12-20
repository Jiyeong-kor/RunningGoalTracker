package com.jeong.runninggoaltracker.feature.reminder.di

import com.jeong.runninggoaltracker.domain.repository.RunningReminderRepository
import com.jeong.runninggoaltracker.domain.usecase.CreateDefaultReminderUseCase
import com.jeong.runninggoaltracker.domain.usecase.DeleteRunningReminderUseCase
import com.jeong.runninggoaltracker.domain.usecase.GetRunningRemindersUseCase
import com.jeong.runninggoaltracker.domain.usecase.ToggleReminderDayUseCase
import com.jeong.runninggoaltracker.domain.usecase.UpsertRunningReminderUseCase
import com.jeong.runninggoaltracker.feature.reminder.alarm.ReminderScheduler
import com.jeong.runninggoaltracker.feature.reminder.alarm.ReminderSchedulerCoordinator
import com.jeong.runninggoaltracker.feature.reminder.alarm.ReminderSchedulingInteractor
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
object ReminderModule {

    @Provides
    @ViewModelScoped
    fun provideDeleteRunningReminderUseCase(
        repository: RunningReminderRepository
    ): DeleteRunningReminderUseCase = DeleteRunningReminderUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideGetRunningRemindersUseCase(
        repository: RunningReminderRepository
    ): GetRunningRemindersUseCase = GetRunningRemindersUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideCreateDefaultReminderUseCase(): CreateDefaultReminderUseCase =
        CreateDefaultReminderUseCase()

    @Provides
    @ViewModelScoped
    fun provideToggleReminderDayUseCase(): ToggleReminderDayUseCase = ToggleReminderDayUseCase()

    @Provides
    @ViewModelScoped
    fun provideUpsertRunningReminderUseCase(
        repository: RunningReminderRepository
    ): UpsertRunningReminderUseCase = UpsertRunningReminderUseCase(repository)

    @Provides
    @ViewModelScoped
    fun provideReminderSchedulingInteractor(
        upsertRunningReminderUseCase: UpsertRunningReminderUseCase,
        deleteRunningReminderUseCase: DeleteRunningReminderUseCase,
        reminderScheduler: ReminderScheduler
    ): ReminderSchedulingInteractor = ReminderSchedulingInteractor(
        upsertRunningReminderUseCase = upsertRunningReminderUseCase,
        deleteRunningReminderUseCase = deleteRunningReminderUseCase,
        reminderScheduler = reminderScheduler
    )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ReminderSingletonModule {

    @Binds
    @Singleton
    abstract fun bindReminderScheduler(
        coordinator: ReminderSchedulerCoordinator
    ): ReminderScheduler
}
