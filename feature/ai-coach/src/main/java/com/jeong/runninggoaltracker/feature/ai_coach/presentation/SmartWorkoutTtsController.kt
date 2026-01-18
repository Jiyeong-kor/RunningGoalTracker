package com.jeong.runninggoaltracker.feature.ai_coach.presentation

import android.content.Context
import android.speech.tts.TextToSpeech
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutSpeechContract

class SmartWorkoutTtsController(context: Context) {
    private var isReady: Boolean = false
    private val tts = TextToSpeech(context.applicationContext) { status ->
        isReady = status == TextToSpeech.SUCCESS
    }

    fun speak(message: String) {
        if (isReady) {
            tts.speak(
                message,
                TextToSpeech.QUEUE_FLUSH,
                null,
                SmartWorkoutSpeechContract.TTS_UTTERANCE_ID
            )
        }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
