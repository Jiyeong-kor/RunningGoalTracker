package com.jeong.runninggoaltracker.data.local

import android.content.Context
import androidx.room.Room
import com.jeong.runninggoaltracker.data.contract.RunningDatabaseContract
import javax.inject.Inject

class RunningDatabaseFactory @Inject constructor() {

    fun create(context: Context): RunningDatabase =
        Room.databaseBuilder(
            context,
            RunningDatabase::class.java,
            RunningDatabaseContract.DATABASE_NAME
        ).fallbackToDestructiveMigration(false)
            .build()
}
