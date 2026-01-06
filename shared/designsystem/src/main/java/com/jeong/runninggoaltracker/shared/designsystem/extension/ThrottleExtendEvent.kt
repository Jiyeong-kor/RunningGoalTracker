package com.jeong.runninggoaltracker.shared.designsystem.extension

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

fun Modifier.throttleClick(
    intervalMillis: Long = 500L,
    onIgnored: () -> Unit = {},
    onClick: () -> Unit
): Modifier = composed {
    val latestOnClick by rememberUpdatedState(onClick)
    val latestOnIgnored by rememberUpdatedState(onIgnored)

    val eventFlow = remember {
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    val lastExecutionTime = remember { mutableLongStateOf(0L) }

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
    intervalMillis: Long = 500L,
    onIgnored: () -> Unit = {},
    onClick: () -> Unit
): () -> Unit {
    val latestOnClick by rememberUpdatedState(onClick)
    val latestOnIgnored by rememberUpdatedState(onIgnored)

    val eventFlow = remember {
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    }

    val lastExecutionTime = remember { mutableLongStateOf(0L) }

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
