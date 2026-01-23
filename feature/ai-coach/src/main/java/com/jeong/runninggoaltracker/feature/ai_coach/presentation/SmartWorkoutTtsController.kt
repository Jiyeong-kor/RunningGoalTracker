package com.jeong.runninggoaltracker.feature.ai_coach.presentation

import android.content.Context
import android.speech.tts.TextToSpeech
import com.jeong.runninggoaltracker.feature.ai_coach.contract.SmartWorkoutSpeechContract
import java.util.Locale

class SmartWorkoutTtsController(context: Context) {
    private var isReady: Boolean = false
    private var pendingMessage: String? = null
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        isReady = status == TextToSpeech.SUCCESS
        if (isReady) {
            tts.setLanguage(Locale.KOREAN)
            pendingMessage?.let { message ->
                pendingMessage = null
                speakInternal(message)
            }
        }
    }

    fun speak(message: String) {
        if (isReady) {
            speakInternal(message)
        } else {
            pendingMessage = message
        }
    }

    private fun speakInternal(message: String) {
        tts.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            SmartWorkoutSpeechContract.TTS_UTTERANCE_ID
        )
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
