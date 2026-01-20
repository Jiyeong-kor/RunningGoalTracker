package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.contract.SQUAT_FLOAT_ZERO
import com.jeong.runninggoaltracker.domain.contract.SQUAT_INT_ZERO
import com.jeong.runninggoaltracker.domain.model.ExerciseType
import com.jeong.runninggoaltracker.domain.model.PoseAnalysisResult
import com.jeong.runninggoaltracker.domain.model.PoseFrame
import com.jeong.runninggoaltracker.domain.model.PostureFeedback
import com.jeong.runninggoaltracker.domain.model.PostureFeedbackType
import com.jeong.runninggoaltracker.domain.model.RepCount
import com.jeong.runninggoaltracker.domain.usecase.lunge.LungeAnalyzer
import com.jeong.runninggoaltracker.domain.usecase.squat.SquatAnalyzer
import javax.inject.Inject

interface ExerciseAnalyzer {
    fun analyze(frame: PoseFrame): PoseAnalysisResult
}

class ProcessPoseUseCase @Inject constructor() {
    private val analyzers: Map<ExerciseType, ExerciseAnalyzer> = mapOf(
        ExerciseType.SQUAT to SquatAnalyzer(),
        ExerciseType.LUNGE to LungeAnalyzer(),
        ExerciseType.PUSH_UP to SquatAnalyzer()
    )

    fun analyze(frame: PoseFrame, exerciseType: ExerciseType): PoseAnalysisResult =
        analyzers[exerciseType]?.analyze(frame)
            ?: PoseAnalysisResult(
                repCount = RepCount(SQUAT_INT_ZERO, isIncremented = false),
                feedback = PostureFeedback(
                    type = PostureFeedbackType.UNKNOWN,
                    isValid = false,
                    accuracy = SQUAT_FLOAT_ZERO,
                    isPerfectForm = false
                ),
                feedbackEvent = null,
                frameMetrics = null,
                repSummary = null,
                lungeRepSummary = null,
                warningEvent = null,
                feedbackKeys = emptyList(),
                skippedLowConfidence = false
            )
}
