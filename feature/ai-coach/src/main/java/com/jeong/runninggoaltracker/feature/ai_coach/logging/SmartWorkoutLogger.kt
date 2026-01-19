package com.jeong.runninggoaltracker.feature.ai_coach.logging

import android.util.Log
import com.jeong.runninggoaltracker.feature.ai_coach.BuildConfig
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutLogContract

object SmartWorkoutLogger {
    fun logDebug(message: () -> String) {
        if (BuildConfig.DEBUG) {
            Log.d(SmartWorkoutLogContract.LOG_TAG, message())
        }
    }
}
