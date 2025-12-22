package com.jeong.runninggoaltracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunningReminderDao {

    @Query("SELECT * FROM running_reminder ORDER BY id ASC")
    fun getAllReminders(): Flow<List<RunningReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminder(reminder: RunningReminderEntity)

    @Query("DELETE FROM running_reminder WHERE id = :reminderId")
    suspend fun deleteReminder(reminderId: Int)
}
