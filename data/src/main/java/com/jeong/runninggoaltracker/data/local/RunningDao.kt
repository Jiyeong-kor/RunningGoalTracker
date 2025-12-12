package com.jeong.runninggoaltracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunningDao {

    @Query("SELECT * FROM running_record ORDER BY date DESC")
    fun getAllRecords(): Flow<List<RunningRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RunningRecordEntity)

    @Query("SELECT * FROM running_goal WHERE id = 0")
    fun getGoal(): Flow<RunningGoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: RunningGoalEntity)

    @Query("SELECT * FROM running_reminder ORDER BY id ASC")
    fun getAllReminders(): Flow<List<RunningReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminder(reminder: RunningReminderEntity)

    @Query("DELETE FROM running_reminder WHERE id = :reminderId")
    suspend fun deleteReminder(reminderId: Int)
}
