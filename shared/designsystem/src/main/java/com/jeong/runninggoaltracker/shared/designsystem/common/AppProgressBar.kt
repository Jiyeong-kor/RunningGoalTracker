package com.jeong.runninggoaltracker.shared.designsystem.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import com.jeong.runninggoaltracker.shared.designsystem.R

@Composable
fun AppProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp? = null,
    backgroundColor: Color? = null,
    foregroundColor: Color = MaterialTheme.colorScheme.primary
) {
    val resolvedHeight = height ?: dimensionResource(R.dimen.progress_bar_height)
    val percentScale = integerResource(R.integer.percentage_scale).toFloat()
    val cornerDivisor = integerResource(R.integer.progress_bar_corner_divisor).toFloat()
    val backgroundAlpha =
        integerResource(R.integer.progress_bar_background_alpha_percent).toFloat() / percentScale
    val resolvedBackgroundColor =
        backgroundColor ?: MaterialTheme.colorScheme.surfaceContainer.copy(alpha = backgroundAlpha)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(resolvedHeight)
            .clip(RoundedCornerShape(resolvedHeight / cornerDivisor))
            .background(resolvedBackgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(foregroundColor)
        )
    }
}
