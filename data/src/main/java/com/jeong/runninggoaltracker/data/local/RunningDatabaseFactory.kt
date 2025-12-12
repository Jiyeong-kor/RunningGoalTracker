package com.jeong.runninggoaltracker.data.local

import android.content.Context
import androidx.room.Room
import javax.inject.Inject

class RunningDatabaseFactory @Inject constructor() {

    fun create(context: Context): RunningDatabase =
        Room.databaseBuilder(
            context,
            RunningDatabase::class.java,
            RunningDatabase.NAME
        ).fallbackToDestructiveMigration(false)
            .build()
}
