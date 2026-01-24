package com.jeong.runninggoaltracker.feature.ai_coach.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jeong.runninggoaltracker.domain.model.ExerciseType
import com.jeong.runninggoaltracker.domain.model.LungeDebugInfo
import com.jeong.runninggoaltracker.domain.model.LungeKneeAngleOutlierReason
import com.jeong.runninggoaltracker.domain.model.PoseSide
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.SquatFrameMetrics
import com.jeong.runninggoaltracker.feature.ai_coach.R
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingMd
import com.jeong.runninggoaltracker.shared.designsystem.theme.appSpacingSm
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextMutedColor
import com.jeong.runninggoaltracker.shared.designsystem.theme.appTextPrimaryColor
import com.jeong.runninggoaltracker.shared.designsystem.R as DesignSystemR

@Composable
fun LungeDebugOverlay(
    debugInfo: LungeDebugInfo?,
    snapshot: LungeRepSnapshot?,
    frameMetrics: SquatFrameMetrics?,
    modifier: Modifier = Modifier
) {
    val textPrimary = appTextPrimaryColor()
    val textMuted = appTextMutedColor()
    val textSize = dimensionResource(R.dimen.smart_workout_accuracy_label_text_size)
    val emptyText = stringResource(R.string.smart_workout_debug_lunge_empty)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(dimensionResource(DesignSystemR.dimen.card_corner_radius)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.92f)
        )
    ) {
        Box(modifier = Modifier.padding(PaddingValues(appSpacingMd()))) {
            Column(verticalArrangement = Arrangement.spacedBy(appSpacingSm())) {
                Text(
                    text = stringResource(R.string.smart_workout_debug_lunge_title),
                    color = textPrimary,
                    fontSize = textSize.value.sp,
                    fontWeight = FontWeight.Bold
                )

                frameMetrics?.let { metrics ->
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_live_knee_raw,
                            metrics.kneeAngleRaw
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_live_knee_ema,
                            metrics.kneeAngleEma
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_live_trunk_raw,
                            metrics.trunkTiltVerticalAngleRaw
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_live_trunk_ema,
                            metrics.trunkTiltVerticalAngleEma
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_rep_min_knee_tracking,
                            metrics.repMinKneeAngle
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_rep_trunk_max_tracking,
                            metrics.repMaxTrunkTiltVerticalAngle
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                }

                debugInfo?.let { info ->
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_active_side,
                            poseSideText(info.activeSide)
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_state,
                            info.state.name
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_phase,
                            info.phase.name
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_reliable,
                            booleanText(info.isReliable)
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_counting_side,
                            poseSideText(info.countingSide)
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_last_knee_angles,
                            info.lastLeftKneeAngle ?: 0f,
                            info.lastRightKneeAngle ?: 0f
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_feedback_key,
                            info.feedbackEventKey?.let { lungeFeedbackLabel(it) } ?: emptyText
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_stability_eligible,
                            booleanText(info.stabilityEligible)
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_hip_samples,
                            info.hipSampleCount
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_shoulder_samples,
                            info.shoulderSampleCount
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_metrics_null_rate,
                            info.metricsNullRate,
                            info.metricsNullStreak
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_thresholds,
                            info.standingThreshold,
                            info.descendingThreshold,
                            info.bottomThreshold,
                            info.ascendingThreshold,
                            info.repCompleteThreshold
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_hysteresis_frames,
                            info.hysteresisFrames
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_hysteresis_counts,
                            info.standingToDescendingCount,
                            info.descendingToBottomCount,
                            info.descendingToStandingCount,
                            info.bottomToAscendingCount,
                            info.ascendingToCompleteCount,
                            info.repCompleteToStandingCount,
                            info.repCompleteToDescendingCount
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    info.hipCenterX?.let { hipCenterX ->
                        val hipMin = info.hipCenterMin ?: hipCenterX
                        val hipMax = info.hipCenterMax ?: hipCenterX
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_hip_center,
                                hipCenterX,
                                hipMin,
                                hipMax
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    info.shoulderCenterX?.let { shoulderCenterX ->
                        val shoulderMin = info.shoulderCenterMin ?: shoulderCenterX
                        val shoulderMax = info.shoulderCenterMax ?: shoulderCenterX
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_shoulder_center,
                                shoulderCenterX,
                                shoulderMin,
                                shoulderMax
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    if (!info.stabilityNormalized) {
                        Text(
                            text = stringResource(R.string.smart_workout_debug_lunge_normalization_warning),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = textSize.value.sp
                        )
                    }
                    val outlierText = listOfNotNull(
                        info.leftOutlierReason?.let { reason ->
                            stringResource(
                                R.string.smart_workout_debug_lunge_outlier_left,
                                outlierReasonText(reason)
                            )
                        },
                        info.rightOutlierReason?.let { reason ->
                            stringResource(
                                R.string.smart_workout_debug_lunge_outlier_right,
                                outlierReasonText(reason)
                            )
                        }
                    ).joinToString(
                        separator = stringResource(R.string.smart_workout_debug_lunge_outlier_separator)
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_outlier_reason,
                            outlierText.ifEmpty { emptyText }
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_rep_min_update,
                            booleanText(info.repMinUpdated)
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                }

                Text(
                    text = stringResource(R.string.smart_workout_debug_lunge_last_rep_title),
                    color = textPrimary,
                    fontSize = textSize.value.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (snapshot == null) {
                    Text(
                        text = emptyText,
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_rep_timestamp,
                            snapshot.timestampMs
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_active_side,
                            poseSideText(snapshot.activeSide)
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_counting_side,
                            poseSideText(snapshot.countingSide)
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_feedback_event,
                            snapshot.feedbackType?.name ?: emptyText
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_feedback_key,
                            snapshot.feedbackEventKey?.let { lungeFeedbackLabel(it) } ?: emptyText
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    val feedbackKeysText = snapshot.feedbackKeys
                        .map { lungeFeedbackLabel(it) }
                        .joinToString(separator = stringResource(R.string.smart_workout_debug_lunge_key_separator))
                        .ifEmpty { emptyText }
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_feedback_keys,
                            feedbackKeysText
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                    snapshot.overallScore?.let { score ->
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_overall_score,
                                score
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    snapshot.frontKneeMinAngle?.let { angle ->
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_front_knee_min,
                                angle
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    snapshot.backKneeMinAngle?.let { angle ->
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_back_knee_min,
                                angle
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    snapshot.maxTorsoLeanAngle?.let { angle ->
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_max_torso_lean,
                                angle
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    snapshot.stabilityStdDev?.let { value ->
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_stability_stddev,
                                value
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    snapshot.maxKneeForwardRatio?.let { ratio ->
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_knee_forward_max,
                                ratio
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    snapshot.maxKneeCollapseRatio?.let { ratio ->
                        Text(
                            text = stringResource(
                                R.string.smart_workout_debug_lunge_knee_collapse_max,
                                ratio
                            ),
                            color = textMuted,
                            fontSize = textSize.value.sp
                        )
                    }
                    Text(
                        text = stringResource(
                            R.string.smart_workout_debug_lunge_good_form_reason,
                            goodFormReasonText(snapshot.goodFormReason)
                        ),
                        color = textMuted,
                        fontSize = textSize.value.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun poseSideText(side: PoseSide?): String = when (side) {
    PoseSide.LEFT -> stringResource(R.string.smart_workout_debug_lunge_side_left)
    PoseSide.RIGHT -> stringResource(R.string.smart_workout_debug_lunge_side_right)
    null -> stringResource(R.string.smart_workout_debug_lunge_side_unknown)
}

@Composable
private fun booleanText(value: Boolean): String =
    if (value) {
        stringResource(R.string.smart_workout_debug_on)
    } else {
        stringResource(R.string.smart_workout_debug_off)
    }

@Composable
private fun outlierReasonText(reason: LungeKneeAngleOutlierReason): String = when (reason) {
    LungeKneeAngleOutlierReason.LOW_RANGE -> stringResource(R.string.smart_workout_debug_lunge_outlier_low)
    LungeKneeAngleOutlierReason.HIGH_RANGE -> stringResource(R.string.smart_workout_debug_lunge_outlier_high)
    LungeKneeAngleOutlierReason.JUMP -> stringResource(R.string.smart_workout_debug_lunge_outlier_jump)
}

@Composable
private fun goodFormReasonText(reason: LungeGoodFormReason): String = when (reason) {
    LungeGoodFormReason.NO_SUMMARY -> stringResource(
        R.string.smart_workout_debug_lunge_good_form_reason_no_summary
    )

    LungeGoodFormReason.FEEDBACK_KEYS_PRESENT -> stringResource(
        R.string.smart_workout_debug_lunge_good_form_reason_keys
    )

    LungeGoodFormReason.GOOD_FORM -> stringResource(
        R.string.smart_workout_debug_lunge_good_form_reason_good
    )
}

@Composable
private fun lungeFeedbackLabel(key: String): String {
    val feedbackLabel = stringResource(
        FeedbackStringMapper.feedbackResId(
            exerciseType = ExerciseType.LUNGE,
            feedbackType = PostureFeedbackType.UNKNOWN,
            feedbackKey = key
        )
    )
    return stringResource(
        R.string.smart_workout_debug_lunge_feedback_label,
        key,
        feedbackLabel
    )
}
