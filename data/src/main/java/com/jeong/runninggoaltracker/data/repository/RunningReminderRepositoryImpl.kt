package com.jeong.runninggoaltracker.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.jeong.runninggoaltracker.data.local.RunningDao
import com.jeong.runninggoaltracker.data.local.toDomain
import com.jeong.runninggoaltracker.data.local.toEntity
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import com.jeong.runninggoaltracker.domain.repository.RunningReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RunningReminderRepositoryImpl @Inject constructor(
    private val dao: RunningDao
) : RunningReminderRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getAllReminders(): Flow<List<RunningReminder>> =
        dao.getAllReminders().map { reminders ->
            reminders.map { it.toDomain() }
        }


    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun upsertReminder(reminder: RunningReminder) {
        dao.upsertReminder(reminder.toEntity())
    }

    override suspend fun deleteReminder(reminderId: Int) {
        dao.deleteReminder(reminderId)
    }
}
