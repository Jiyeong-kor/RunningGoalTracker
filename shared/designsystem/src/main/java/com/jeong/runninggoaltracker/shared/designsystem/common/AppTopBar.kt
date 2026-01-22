package com.jeong.runninggoaltracker.shared.designsystem.common

import androidx.annotation.StringRes
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.jeong.runninggoaltracker.shared.designsystem.R
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    @StringRes titleResId: Int?,
    modifier: Modifier = Modifier,
    @StringRes fallbackTitleResId: Int? = null,
    onBack: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography

    val title = titleResId?.let { stringResource(id = it) }
        ?: fallbackTitleResId?.let { stringResource(id = it) }

    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            if (title != null) {
                Text(
                    text = title,
                    style = typography.titleMedium
                )
            }
        },
        navigationIcon = {
            if (onBack != null) {
                val onBackClick = rememberThrottleClick(onClick = onBack)
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(
                            R.string.designsystem_app_top_bar_back
                        )
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorScheme.surface,
            titleContentColor = colorScheme.onSurface
        )
    )
}

@Preview(showBackground = true)
@Composable
fun AppTopBarPreview() =
    RunningGoalTrackerTheme {
        AppTopBar(
            titleResId = R.string.designsystem_app_top_bar_preview_title
        )
    }
