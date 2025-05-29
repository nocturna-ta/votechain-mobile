package com.nocturna.votechain.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Accessibility Manager for Text-to-Speech functionality
 * Provides audio feedback for visually impaired users
 */
class AccessibilityManager private constructor(private val context: Context) {
    private val TAG = "AccessibilityManager"

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: AccessibilityManager? = null

        fun getInstance(context: Context): AccessibilityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AccessibilityManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Initialize Text-to-Speech engine
     */
    fun initialize() {
        if (isInitialized) return

        Log.d(TAG, "Initializing Text-to-Speech engine")

        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale("id", "ID")) // Indonesian

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to English if Indonesian is not available
                    textToSpeech?.setLanguage(Locale.ENGLISH)
                    Log.w(TAG, "Indonesian language not supported, using English")
                }

                // Set up utterance progress listener
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        Log.d(TAG, "TTS started speaking: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        Log.d(TAG, "TTS finished speaking: $utteranceId")
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        Log.e(TAG, "TTS error for utterance: $utteranceId")
                    }
                })

                isInitialized = true
                _isEnabled.value = true
                Log.d(TAG, "Text-to-Speech initialized successfully")

                // Welcome message
                speakText("Sistem suara VoteChain aktif. Aplikasi siap digunakan dengan bantuan suara.")

            } else {
                Log.e(TAG, "Text-to-Speech initialization failed")
                _isEnabled.value = false
            }
        }
    }

    /**
     * Speak text with TTS
     */
    fun speakText(text: String, priority: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isInitialized || textToSpeech == null) {
            Log.w(TAG, "TTS not initialized, cannot speak: $text")
            return
        }

        Log.d(TAG, "Speaking: $text")

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_${System.currentTimeMillis()}")
        }

        textToSpeech?.speak(text, priority, params, "tts_${System.currentTimeMillis()}")
    }

    /**
     * Stop current speech
     */
    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
        Log.d(TAG, "TTS stopped")
    }

    /**
     * Enable/disable accessibility features
     */
    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        if (!enabled) {
            stopSpeaking()
        }
        Log.d(TAG, "Accessibility ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Speak candidate information
     */
    fun speakCandidateInfo(candidateNumber: Int, presidentName: String, vicePresidentName: String) {
        val text = "Kandidat nomor $candidateNumber. Calon Presiden: $presidentName. Calon Wakil Presiden: $vicePresidentName."
        speakText(text)
    }

    /**
     * Speak candidate selection
     */
    fun speakCandidateSelection(candidateNumber: Int, presidentName: String) {
        val text = "Anda memilih kandidat nomor $candidateNumber, $presidentName"
        speakText(text)
    }

    /**
     * Speak navigation information
     */
    fun speakNavigation(screenName: String) {
        val text = "Halaman $screenName"
        speakText(text)
    }

    /**
     * Speak button or action information
     */
    fun speakAction(action: String) {
        speakText(action)
    }

    /**
     * Speak form field information
     */
    fun speakFormField(fieldName: String, instruction: String? = null) {
        val text = if (instruction != null) {
            "$fieldName. $instruction"
        } else {
            fieldName
        }
        speakText(text)
    }

    /**
     * Speak validation error
     */
    fun speakError(errorMessage: String) {
        val text = "Kesalahan: $errorMessage"
        speakText(text, TextToSpeech.QUEUE_FLUSH) // Interrupt current speech for errors
    }

    /**
     * Speak success message
     */
    fun speakSuccess(message: String) {
        val text = "Berhasil: $message"
        speakText(text)
    }

    /**
     * Clean up resources
     */
    fun shutdown() {
        Log.d(TAG, "Shutting down TTS")
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        _isEnabled.value = false
        INSTANCE = null
    }

    /**
     * Check if TTS is available on device
     */
    fun isTTSAvailable(): Boolean {
        return isInitialized && textToSpeech != null
    }
}