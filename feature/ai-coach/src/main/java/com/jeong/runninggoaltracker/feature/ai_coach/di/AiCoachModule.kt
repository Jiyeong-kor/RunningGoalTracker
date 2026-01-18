package com.jeong.runninggoaltracker.feature.ai_coach.di

import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.jeong.runninggoaltracker.feature.ai_coach.data.pose.MlKitPoseDetector
import com.jeong.runninggoaltracker.feature.ai_coach.data.pose.PoseDetector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object AiCoachModule {
    @Provides
    @ViewModelScoped
    fun providePoseDetector(): PoseDetector {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        return MlKitPoseDetector(
            poseDetector = PoseDetection.getClient(options),
            isFrontCamera = true
        )
    }
}
