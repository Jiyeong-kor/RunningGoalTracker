package com.jeong.runninggoaltracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jeong.runninggoaltracker.feature.record.recognition.ActivityRecognitionMonitor
import com.jeong.runninggoaltracker.presentation.navigation.AppNavGraph
import com.jeong.runninggoaltracker.presentation.navigation.bottomNavItems
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var activityRecognitionMonitor: ActivityRecognitionMonitor

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestActivityRecognitionPermissionIfNeeded()

        setContent {
            RunningGoalTrackerTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                Scaffold(
                    topBar = {
                        AppTopBar(currentRoute = currentRoute)
                    },
                    bottomBar = {
                        BottomNavBar(navController = navController)
                    }
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        activityRecognitionMonitor = activityRecognitionMonitor
                    )
                }
            }
        }
    }

    private fun requestActivityRecognitionPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val permission = Manifest.permission.ACTIVITY_RECOGNITION

            val granted = ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    REQUEST_CODE_ACTIVITY_RECOGNITION
                )
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_ACTIVITY_RECOGNITION = 3001
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    currentRoute: String
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val title = when (currentRoute) {
        "home" -> stringResource(R.string.title_home)
        "record" -> stringResource(R.string.title_record)
        "goal" -> stringResource(R.string.title_goal)
        "reminder" -> stringResource(R.string.title_reminder)
        else -> stringResource(R.string.app_name_full)
    }

    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = typography.titleMedium
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surface,
            titleContentColor = colorScheme.onSurface
        )
    )
}

@Composable
private fun BottomNavBar(
    navController: NavHostController
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = colorScheme.surface
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route

            val icon = when (item.route) {
                "home" -> Icons.Filled.Home
                "record" -> Icons.AutoMirrored.Filled.DirectionsRun
                "reminder" -> Icons.Filled.Notifications
                else -> Icons.Filled.Home
            }

            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(imageVector = icon, contentDescription = stringResource(item.labelResId))
                },
                label = {
                    Text(text = stringResource(item.labelResId), style = typography.labelSmall)
                }
            )
        }
    }
}
