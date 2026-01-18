package com.jeong.runninggoaltracker.domain.usecase.squat

import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.SquatFormGrade
import com.jeong.runninggoaltracker.domain.model.SquatFormIssue
import com.jeong.runninggoaltracker.domain.model.SquatRepSummary

class SquatQualityEvaluator {
    fun feedbackType(summary: SquatRepSummary): PostureFeedbackType {
        val issues = summary.issues
        if (issues.contains(SquatFormIssue.INSUFFICIENT_DEPTH)) {
            return PostureFeedbackType.TOO_SHALLOW
        }
        if (summary.grade == SquatFormGrade.OK && issues.isEmpty()) {
            return PostureFeedbackType.GOOD_FORM
        }
        return when {
            issues.contains(SquatFormIssue.EXCESS_TRUNK_LEAN_HARD) ->
                PostureFeedbackType.EXCESS_FORWARD_LEAN

            issues.contains(SquatFormIssue.HEEL_RISE) -> PostureFeedbackType.HEEL_RISE
            issues.contains(SquatFormIssue.KNEE_FORWARD_TRANSLATION) -> PostureFeedbackType.KNEE_FORWARD
            summary.grade == SquatFormGrade.BAD_FORM -> PostureFeedbackType.TOO_SHALLOW
            else -> PostureFeedbackType.GOOD_FORM
        }
    }
}
