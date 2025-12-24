package com.jeong.runninggoaltracker.domain.usecase

import com.jeong.runninggoaltracker.domain.model.RunningSummary
import com.jeong.runninggoaltracker.domain.repository.RunningGoalRepository
import com.jeong.runninggoaltracker.domain.repository.RunningRecordRepository
import com.jeong.runninggoaltracker.domain.util.DateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetRunningSummaryUseCase @Inject constructor(
    private val goalRepository: RunningGoalRepository,
    private val recordRepository: RunningRecordRepository,
    private val dateProvider: DateProvider,
    private val summaryCalculator: RunningSummaryCalculator
) {
    operator fun invoke(): Flow<RunningSummary> = combine(
        goalRepository.getGoal(),
        recordRepository.getAllRecords(),
        dateProvider.getTodayFlow()
    ) { goal, records, today ->
        summaryCalculator.calculate(
            goal = goal,
            records = records,
            todayMillis = today
        )
    }
}
