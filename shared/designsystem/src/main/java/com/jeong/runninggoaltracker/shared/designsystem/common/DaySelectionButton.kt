package com.jeong.runninggoaltracker.shared.designsystem.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.jeong.runninggoaltracker.shared.designsystem.extension.throttleClick

@Composable
fun DaySelectionButton(
    dayName: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    textStyle: TextStyle = MaterialTheme.typography.labelLarge,
    intervalMillis: Long = 500L,
    onIgnored: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val backgroundColor = if (isSelected) colorScheme.primary else Color.Transparent
    val contentColor = if (isSelected) colorScheme.onPrimary else colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .throttleClick(
                intervalMillis = intervalMillis,
                onIgnored = onIgnored,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayName,
                color = contentColor,
                style = textStyle
            )
        }
    }
}
