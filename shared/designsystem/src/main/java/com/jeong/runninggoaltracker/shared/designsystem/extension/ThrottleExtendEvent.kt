package com.jeong.runninggoaltracker.shared.designsystem.extension

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.jeong.runninggoaltracker.shared.designsystem.config.NumericResourceProvider
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun Modifier.throttleClick(
    intervalMillis: Long = throttleClickIntervalMillis(),
    onIgnored: () -> Unit = {},
    onClick: () -> Unit
): Modifier = composed {
    val latestOnClick by rememberUpdatedState(onClick)
    val latestOnIgnored by rememberUpdatedState(onIgnored)

    val eventFlow = remember {
        MutableSharedFlow<Unit>(
            extraBufferCapacity = FLOW_BUFFER_CAPACITY,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    val lastExecutionTime = remember { mutableLongStateOf(INITIAL_EXECUTION_TIME_MILLIS) }

    LaunchedEffect(Unit) {
        eventFlow.collect {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastExecutionTime.longValue > intervalMillis) {
                lastExecutionTime.longValue = currentTime
                latestOnClick()
            } else {
                latestOnIgnored()
            }
        }
    }

    this.clickable { eventFlow.tryEmit(Unit) }
}

@Composable
fun rememberThrottleClick(
    intervalMillis: Long = throttleClickIntervalMillis(),
    onIgnored: () -> Unit = {},
    onClick: () -> Unit
): () -> Unit {
    val latestOnClick by rememberUpdatedState(onClick)
    val latestOnIgnored by rememberUpdatedState(onIgnored)

    val eventFlow = remember {
        MutableSharedFlow<Unit>(
            extraBufferCapacity = FLOW_BUFFER_CAPACITY,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    val lastExecutionTime = remember { mutableLongStateOf(INITIAL_EXECUTION_TIME_MILLIS) }

    LaunchedEffect(Unit) {
        eventFlow.collect {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastExecutionTime.longValue > intervalMillis) {
                lastExecutionTime.longValue = currentTime
                latestOnClick()
            } else {
                latestOnIgnored()
            }
        }
    }

    return remember(eventFlow) { { eventFlow.tryEmit(Unit) } }
}

@Composable
private fun throttleClickIntervalMillis(): Long =
    NumericResourceProvider.throttleClickIntervalMillis(LocalContext.current)

private const val FLOW_BUFFER_CAPACITY = 1
private const val INITIAL_EXECUTION_TIME_MILLIS = 0L
