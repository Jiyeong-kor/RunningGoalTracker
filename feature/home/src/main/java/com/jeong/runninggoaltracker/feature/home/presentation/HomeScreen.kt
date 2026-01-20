package com.jeong.runninggoaltracker.feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.annotation.IntegerRes
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import com.jeong.runninggoaltracker.feature.home.R
import com.jeong.runninggoaltracker.feature.home.contract.HomeDateTimeContract
import com.jeong.runninggoaltracker.shared.designsystem.common.AppContentCard
import com.jeong.runninggoaltracker.shared.designsystem.common.AppProgressBar
import com.jeong.runninggoaltracker.shared.designsystem.common.AppSurfaceCard
import com.jeong.runninggoaltracker.shared.designsystem.extension.rememberThrottleClick
import com.jeong.runninggoaltracker.shared.designsystem.extension.throttleClick
import com.jeong.runninggoaltracker.shared.designsystem.theme.RunningGoalTrackerTheme
import com.jeong.runninggoaltracker.shared.designsystem.theme.appAccentColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appBackgroundColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingMd
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingSm
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSurfaceColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextMutedColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextPrimaryColor
import kotlinx.coroutines.flow.Flow

@Composable
fun HomeRoute(
    activityStateFlow: Flow<ActivityRecognitionUiState>,
    activityLogsFlow: Flow<List<ActivityLogUiModel>>,
    onNavigateToRecord: () -> Unit,
    onNavigateToGoal: () -> Unit,
    onNavigateToReminder: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentActivityMaxCount = integerResource(id = R.integer.home_recent_activity_max_count)

    LaunchedEffect(activityStateFlow, activityLogsFlow, recentActivityMaxCount) {
        viewModel.bindActivityFlows(
            activityStateFlow = activityStateFlow,
            activityLogsFlow = activityLogsFlow,
            recentActivityMaxCount = recentActivityMaxCount
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                HomeUiEffect.NavigateToRecord -> onNavigateToRecord()
                HomeUiEffect.NavigateToGoal -> onNavigateToGoal()
                HomeUiEffect.NavigateToReminder -> onNavigateToReminder()
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onRecordClick = viewModel::onRecordClick,
        onGoalClick = viewModel::onGoalClick,
        onReminderClick = viewModel::onReminderClick
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRecordClick: () -> Unit,
    onGoalClick: () -> Unit,
    onReminderClick: () -> Unit
) {
    val accentColor = appAccentColor()
    val backgroundColor = appBackgroundColor()
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()
    val screenHorizontalPadding = dimensionResource(id = R.dimen.home_screen_padding_horizontal)
    val listItemSpacing = dimensionResource(id = R.dimen.home_screen_list_spacing)
    val screenPaddingTop = dimensionResource(id = R.dimen.home_screen_padding_top)
    val screenPaddingBottom = dimensionResource(id = R.dimen.home_screen_padding_bottom)
    val activityLabelResId = uiState.activityLabelResId ?: R.string.activity_unknown

    var isAnonymousBannerVisible by rememberSaveable { mutableStateOf(true) }
    val onRecordClickThrottled = rememberThrottleClick(onClick = onRecordClick)
    val onGoalClickThrottled = rememberThrottleClick(onClick = onGoalClick)
    val onReminderClickThrottled = rememberThrottleClick(onClick = onReminderClick)
    val onDismissBanner = rememberThrottleClick(onClick = { isAnonymousBannerVisible = false })

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = screenHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(listItemSpacing),
        contentPadding = PaddingValues(top = screenPaddingTop, bottom = screenPaddingBottom)
    ) {
        if (isAnonymousBannerVisible) {
            item {
                AppContentCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.shapes.medium
                            )
                            .padding(appSpacingMd()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(integerResource(id = R.integer.home_weight_one).toFloat()),
                            verticalArrangement = Arrangement.spacedBy(appSpacingSm())
                        ) {
                            Text(
                                text = stringResource(R.string.home_anonymous_banner_title),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = stringResource(R.string.home_anonymous_banner_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        IconButton(onClick = onDismissBanner) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
        item {
            WeeklyProgressCard(
                progress = uiState.progress,
                remainingKm = uiState.remainingKm,
                count = uiState.recordCountThisWeek,
                onClick = onGoalClickThrottled
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.home_section_weekly_manage),
                    color = textPrimary,
                    fontSize = dimensionResource(id = R.dimen.home_section_title_text_size).value.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onReminderClickThrottled,
                    colors = ButtonDefaults.textButtonColors(contentColor = accentColor)
                ) {
                    Text(
                        stringResource(R.string.home_action_reminder_settings),
                        fontSize = dimensionResource(id = R.dimen.home_section_action_text_size).value.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = stringResource(R.string.home_section_recent_activity),
                onViewAllClick = onRecordClickThrottled
            )
        }

        if (uiState.recentActivities.isEmpty()) {
            item {
                Text(
                    stringResource(
                        R.string.home_empty_recent_activity_format,
                        stringResource(activityLabelResId)
                    ),
                    color = textMuted,
                    fontSize = dimensionResource(id = R.dimen.home_empty_text_size).value.sp,
                    modifier = Modifier.padding(
                        vertical = dimensionResource(id = R.dimen.home_empty_text_vertical_padding)
                    )
                )
            }
        } else {
            items(uiState.recentActivities) { activity ->
                RecentActivityRow(activity)
            }
        }
    }
}

@Composable
private fun WeeklyProgressCard(
    progress: Float,
    remainingKm: Float,
    count: Int,
    onClick: () -> Unit
) {
    val accentColor = appAccentColor()
    val surfaceColor = appSurfaceColor()
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()
    val cardCornerRadius = dimensionResource(id = R.dimen.home_weekly_card_corner_radius)
    val cardPadding = dimensionResource(id = R.dimen.home_weekly_card_padding)
    val titleTextSize = dimensionResource(id = R.dimen.home_weekly_title_text_size).value.sp
    val progressTextSize = dimensionResource(id = R.dimen.home_weekly_progress_text_size).value.sp
    val progressSpacerSmall = dimensionResource(id = R.dimen.home_weekly_progress_spacer_small)
    val progressSpacerMedium = dimensionResource(id = R.dimen.home_weekly_progress_spacer_medium)
    val progressSpacerLarge = dimensionResource(id = R.dimen.home_weekly_progress_spacer_large)
    val progressIconSize = dimensionResource(id = R.dimen.home_weekly_progress_icon_size)
    val infoItemSpacing = dimensionResource(id = R.dimen.home_weekly_info_item_spacing)
    val percentBase = integerResource(id = R.integer.home_percent_base)

    AppSurfaceCard(
        modifier = Modifier
            .fillMaxWidth()
            .throttleClick(onClick = onClick),
        shape = RoundedCornerShape(cardCornerRadius),
        containerColor = surfaceColor,
        contentPadding = PaddingValues(cardPadding)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(R.string.home_weekly_progress_title),
                        color = textMuted,
                        fontSize = titleTextSize
                    )
                    Spacer(modifier = Modifier.height(progressSpacerSmall))
                    Text(
                        stringResource(
                            R.string.home_weekly_progress_complete_format,
                            (progress * percentBase).toInt()
                        ),
                        color = textPrimary,
                        fontSize = progressTextSize,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = accentColor.copy(
                        alpha = alphaFromPercentResource(
                            R.integer.home_alpha_progress_icon_tint_percent
                        )
                    ),
                    modifier = Modifier.size(progressIconSize)
                )
            }

            Spacer(modifier = Modifier.height(progressSpacerMedium))

            AppProgressBar(progress = progress)

            Spacer(modifier = Modifier.height(progressSpacerLarge))

            Row(horizontalArrangement = Arrangement.spacedBy(infoItemSpacing)) {
                InfoItem(
                    label = stringResource(R.string.home_weekly_remaining_distance),
                    value = stringResource(R.string.home_distance_km_format, remainingKm),
                    modifier = Modifier.weight(integerResource(id = R.integer.home_weight_one).toFloat())
                )
                InfoItem(
                    label = stringResource(R.string.home_weekly_run_count),
                    value = stringResource(R.string.home_weekly_run_count_format, count),
                    modifier = Modifier.weight(integerResource(id = R.integer.home_weight_one).toFloat())
                )
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()
    val cardCornerRadius = dimensionResource(id = R.dimen.home_info_card_corner_radius)
    val cardPadding = dimensionResource(id = R.dimen.home_info_card_padding)
    val labelTextSize = dimensionResource(id = R.dimen.home_info_label_text_size).value.sp
    val valueTextSize = dimensionResource(id = R.dimen.home_info_value_text_size).value.sp

    Column(
        modifier = modifier
            .background(
                Color.White.copy(
                    alpha = alphaFromPercentResource(
                        R.integer.home_alpha_info_card_background_percent
                    )
                ),
                RoundedCornerShape(cardCornerRadius)
            )
            .padding(cardPadding)
    ) {
        Text(label, color = textMuted, fontSize = labelTextSize, fontWeight = FontWeight.Medium)
        Text(value, color = textPrimary, fontSize = valueTextSize, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RecentActivityRow(activity: HomeRecentActivityUiModel) {
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()
    val rowCornerRadius = dimensionResource(id = R.dimen.home_activity_row_corner_radius)
    val rowPadding = dimensionResource(id = R.dimen.home_activity_row_padding)
    val iconContainerSize = dimensionResource(id = R.dimen.home_activity_icon_container_size)
    val iconContainerCornerRadius =
        dimensionResource(id = R.dimen.home_activity_icon_container_corner_radius)
    val iconSize = dimensionResource(id = R.dimen.home_activity_icon_size)
    val rowSpacerWidth = dimensionResource(id = R.dimen.home_activity_row_spacer_width)
    val titleTextSize = dimensionResource(id = R.dimen.home_activity_title_text_size).value.sp
    val subtitleTextSize = dimensionResource(id = R.dimen.home_activity_subtitle_text_size).value.sp
    val dateLabel = formatActivityDateLabel(
        month = activity.month,
        day = activity.day,
        dayOfWeek = activity.dayOfWeek
    )
    val distanceLabel = stringResource(R.string.home_activity_distance_placeholder)
    val timeLabel = stringResource(R.string.home_activity_time_placeholder)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color.White.copy(
                    alpha = alphaFromPercentResource(
                        R.integer.home_alpha_activity_row_background_percent
                    )
                ),
                RoundedCornerShape(rowCornerRadius)
            )
            .padding(rowPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(iconContainerSize)
                .background(
                    Color.White.copy(
                        alpha = alphaFromPercentResource(
                            R.integer.home_alpha_activity_icon_container_percent
                        )
                    ),
                    RoundedCornerShape(iconContainerCornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.DirectionsRun,
                contentDescription = null,
                tint = textMuted,
                modifier = Modifier.size(iconSize)
            )
        }

        Spacer(modifier = Modifier.width(rowSpacerWidth))

        Column(modifier = Modifier.weight(integerResource(id = R.integer.home_weight_one).toFloat())) {
            Text(
                stringResource(
                    R.string.home_activity_title_format,
                    distanceLabel,
                    stringResource(activity.typeResId)
                ),
                color = textPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = titleTextSize
            )
            Text(
                stringResource(
                    R.string.home_activity_subtitle_format,
                    dateLabel,
                    timeLabel
                ),
                color = textMuted,
                fontSize = subtitleTextSize
            )
        }

        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray.copy(
                alpha = alphaFromPercentResource(
                    R.integer.home_alpha_activity_arrow_tint_percent
                )
            )
        )
    }
}

@Composable
private fun SectionHeader(title: String, onViewAllClick: () -> Unit) {
    val accentColor = appAccentColor()
    val textPrimary = appTextPrimaryColor()
    val titleTextSize = dimensionResource(id = R.dimen.home_section_title_text_size).value.sp
    val actionTextSize = dimensionResource(id = R.dimen.home_section_action_text_size).value.sp
    val onViewAllClickThrottled = rememberThrottleClick(onClick = onViewAllClick)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = textPrimary, fontSize = titleTextSize, fontWeight = FontWeight.Bold)
        TextButton(onClick = onViewAllClickThrottled) {
            Text(
                stringResource(R.string.home_action_view_all),
                color = accentColor,
                fontSize = actionTextSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun formatActivityDateLabel(month: Int, day: Int, dayOfWeek: Int): String =
    stringResource(
        R.string.home_date_label_format,
        month,
        day,
        stringResource(dayOfWeekToResId(dayOfWeek))
    )

private fun dayOfWeekToResId(dayOfWeekValue: Int): Int =
    when (dayOfWeekValue) {
        HomeDateTimeContract.DAY_OF_WEEK_MON -> R.string.home_day_of_week_mon
        HomeDateTimeContract.DAY_OF_WEEK_TUE -> R.string.home_day_of_week_tue
        HomeDateTimeContract.DAY_OF_WEEK_WED -> R.string.home_day_of_week_wed
        HomeDateTimeContract.DAY_OF_WEEK_THU -> R.string.home_day_of_week_thu
        HomeDateTimeContract.DAY_OF_WEEK_FRI -> R.string.home_day_of_week_fri
        HomeDateTimeContract.DAY_OF_WEEK_SAT -> R.string.home_day_of_week_sat
        else -> R.string.home_day_of_week_sun
    }

@Composable
private fun alphaFromPercentResource(@IntegerRes percentResId: Int): Float =
    integerResource(id = percentResId).toFloat() /
            integerResource(id = R.integer.home_percent_base).toFloat()

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    val uiState = HomeUiState(
        weeklyGoalKm = 20.0,
        totalThisWeekKm = 12.5,
        remainingKm = 7.5f,
        recordCountThisWeek = 3,
        progress = 0.62f,
        activityLabelResId = R.string.activity_running,
        recentActivities = listOf(
            HomeRecentActivityUiModel(
                timestamp = 1_728_000_000_000,
                month = 10,
                day = 12,
                dayOfWeek = HomeDateTimeContract.DAY_OF_WEEK_MON,
                typeResId = R.string.activity_running
            ),
            HomeRecentActivityUiModel(
                timestamp = 1_727_900_000_000,
                month = 10,
                day = 11,
                dayOfWeek = HomeDateTimeContract.DAY_OF_WEEK_SUN,
                typeResId = R.string.activity_walking
            )
        )
    )

    RunningGoalTrackerTheme {
        HomeScreen(
            uiState = uiState,
            onRecordClick = {},
            onGoalClick = {},
            onReminderClick = {}
        )
    }
}
