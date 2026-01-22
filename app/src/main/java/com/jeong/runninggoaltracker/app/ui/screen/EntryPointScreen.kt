package com.jeong.runninggoaltracker.app.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.jeong.runninggoaltracker.app.ui.navigation.AppNavGraph

@Composable
fun EntryPointScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    AppNavGraph(
        navController = navController,
        modifier = modifier
    )
}
