package com.jeong.runninggoaltracker.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jeong.runninggoaltracker.app.ui.permission.rememberActivityRecognitionPermissionRequester
import com.jeong.runninggoaltracker.app.ui.permission.rememberCameraPermissionRequester
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
            val requestActivityRecognitionPermission =
                rememberActivityRecognitionPermissionRequester()
            val requestCameraPermission = rememberCameraPermissionRequester()
            var onTrackingPermissionResult by remember {
                mutableStateOf<(Boolean) -> Unit>({ _ -> })
            }
            val trackingPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { results ->
                val granted = results.values.all { it }
                onTrackingPermissionResult(granted)
            }

            val requestTrackingPermissions: (onResult: (Boolean) -> Unit) -> Unit =
                { onResult ->
                    onTrackingPermissionResult = onResult
                    val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                    trackingPermissionLauncher.launch(permissions.toTypedArray())
                }

            RunningGoalTrackerTheme {
                EntryPointScreen(
                    activityRecognitionMonitor = activityRecognitionMonitor,
                    requestActivityRecognitionPermission =
                    requestActivityRecognitionPermission,
                    requestTrackingPermissions = requestTrackingPermissions,
                    requestCameraPermission = requestCameraPermission
                )
            }
        }
    }
}
