package com.jeong.runninggoaltracker.data.repository

import android.os.Build
import androidx.test.filters.SdkSuppress
import com.jeong.runninggoaltracker.data.local.RunningGoalDao
import com.jeong.runninggoaltracker.data.local.RunningGoalEntity
import com.jeong.runninggoaltracker.data.local.RunningRecordEntity
import com.jeong.runninggoaltracker.data.local.RunningRecordDao
import com.jeong.runninggoaltracker.data.local.RunningReminderDao
import com.jeong.runninggoaltracker.data.local.RunningReminderEntity
import com.jeong.runninggoaltracker.domain.model.RunningGoal
import com.jeong.runninggoaltracker.domain.model.RunningRecord
import com.jeong.runninggoaltracker.domain.model.RunningReminder
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
class RunningRepositoriesImplTest {

    private val fakeDaos = FakeRunningDaos()
    private val recordRepository = RunningRecordRepositoryImpl(fakeDaos)
    private val goalRepository = RunningGoalRepositoryImpl(fakeDaos)
    private val reminderRepository = RunningReminderRepositoryImpl(fakeDaos)

    @Test
    fun `record repository exposes mapped records and inserts entities`() = runBlocking {
        val recordDate = LocalDate.of(2024, 6, 1)
        fakeDaos.records.value = listOf(
            RunningRecordEntity(
                id = 1L,
                date = recordDate.toString(),
                distanceKm = 4.2,
                durationMinutes = 25
            )
        )

        val records = recordRepository.getAllRecords().first()

        assertEquals(1, records.size)
        assertEquals(recordDate, records.first().date)
        assertEquals(4.2, records.first().distanceKm, 0.0)

        val newRecord = RunningRecord(
            id = 2L,
            date = LocalDate.of(2024, 6, 2),
            distanceKm = 10.0,
            durationMinutes = 50
        )

        recordRepository.addRecord(newRecord)

        assertEquals(
            RunningRecordEntity(
                id = 2L,
                date = "2024-06-02",
                distanceKm = 10.0,
                durationMinutes = 50
            ),
            fakeDaos.lastInsertedRecord
        )
    }

    @Test
    fun `goal repository maps latest goal and upserts entity`() = runBlocking {
        fakeDaos.goal.value = RunningGoalEntity(weeklyGoalKm = 15.5)

        val goal = goalRepository.getGoal().first()

        assertEquals(15.5, goal!!.weeklyGoalKm, 0.0)

        goalRepository.upsertGoal(RunningGoal(weeklyGoalKm = 20.0))

        assertEquals(RunningGoalEntity(weeklyGoalKm = 20.0), fakeDaos.lastUpsertedGoal)
        assertEquals(20.0, fakeDaos.goal.value!!.weeklyGoalKm, 0.0)
    }

    @Test
    fun `reminder repository maps reminders and coordinates dao updates`() = runBlocking {
        fakeDaos.reminders.value = listOf(
            RunningReminderEntity(
                id = 3,
                hour = 6,
                minute = 45,
                enabled = true,
                days = "1,5"
            )
        )

        val reminders = reminderRepository.getAllReminders().first()

        assertEquals(1, reminders.size)
        val reminder = reminders.first()
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), reminder.days)

        val newReminder = RunningReminder(
            id = 4,
            hour = 7,
            minute = 15,
            enabled = true,
            days = setOf(DayOfWeek.TUESDAY)
        )

        reminderRepository.upsertReminder(newReminder)
        reminderRepository.deleteReminder(3)

        assertTrue(fakeDaos.upsertedReminders.contains(
            RunningReminderEntity(
                id = 4,
                hour = 7,
                minute = 15,
                enabled = true,
                days = "2"
            )
        ))
        assertEquals(listOf(3), fakeDaos.deletedReminderIds)
        assertTrue(fakeDaos.reminders.value.none { it.id == 3 })
    }

    private class FakeRunningDaos : RunningRecordDao, RunningGoalDao, RunningReminderDao {
        val records = MutableStateFlow<List<RunningRecordEntity>>(emptyList())
        val goal = MutableStateFlow<RunningGoalEntity?>(null)
        val reminders = MutableStateFlow<List<RunningReminderEntity>>(emptyList())

        var lastInsertedRecord: RunningRecordEntity? = null
        var lastUpsertedGoal: RunningGoalEntity? = null
        val upsertedReminders = mutableListOf<RunningReminderEntity>()
        val deletedReminderIds = mutableListOf<Int>()

        override fun getAllRecords(): Flow<List<RunningRecordEntity>> = records

        override suspend fun insertRecord(record: RunningRecordEntity) {
            lastInsertedRecord = record
            records.value = records.value + record
        }

        override fun getGoal(): Flow<RunningGoalEntity?> = goal

        override suspend fun upsertGoal(goal: RunningGoalEntity) {
            lastUpsertedGoal = goal
            this.goal.value = goal
        }

        override fun getAllReminders(): Flow<List<RunningReminderEntity>> = reminders

        override suspend fun upsertReminder(reminder: RunningReminderEntity) {
            upsertedReminders += reminder
            reminders.value = reminders.value.filterNot { it.id == reminder.id } + reminder
        }

        override suspend fun deleteReminder(reminderId: Int) {
            deletedReminderIds += reminderId
            reminders.value = reminders.value.filterNot { it.id == reminderId }
        }
    }
}
