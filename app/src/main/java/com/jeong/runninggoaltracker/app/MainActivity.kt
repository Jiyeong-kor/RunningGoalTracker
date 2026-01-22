package com.jeong.runninggoaltracker.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jeong.runninggoaltracker.app.ui.screen.EntryPointScreen
import com.jeong.runninggoaltracker.feature.record.api.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityRecognitionMonitor: ActivityRecognitionMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RunningGoalTrackerTheme {
                EntryPointScreen(
                )
            }
        }
    }
}
