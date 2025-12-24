package com.jeong.runninggoaltracker.feature.reminder.alarm

import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.usecase.DeleteRunningReminderUseCase
import com.jeong.runninggoaltracker.domain.usecase.UpsertRunningReminderUseCase
import javax.inject.Inject

class ReminderSchedulingInteractor @Inject constructor(
    private val upsertRunningReminderUseCase: UpsertRunningReminderUseCase,
    private val deleteRunningReminderUseCase: DeleteRunningReminderUseCase,
    private val reminderScheduler: ReminderScheduler,
) {

    suspend fun saveReminder(
        updatedReminder: RunningReminder,
        previousReminder: RunningReminder?,
    ) {
        upsertRunningReminderUseCase(updatedReminder)
        reschedule(
            previousReminder = previousReminder,
            updatedReminder = updatedReminder,
        )
    }

    suspend fun deleteReminder(reminder: RunningReminder) {
        val id = reminder.id ?: return
        reminderScheduler.cancel(reminder)
        deleteRunningReminderUseCase(id)
    }

    private fun reschedule(
        previousReminder: RunningReminder?,
        updatedReminder: RunningReminder,
    ) {
        if (previousReminder == updatedReminder) return

        previousReminder?.let(reminderScheduler::cancel)

        if (updatedReminder.shouldSchedule) {
            reminderScheduler.scheduleIfNeeded(updatedReminder)
        }
    }

    private val RunningReminder.shouldSchedule: Boolean
        get() = enabled && days.isNotEmpty()
}
