package com.nocturna.votechain.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton class to manage language settings throughout the app
 */
object LanguageManager {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    // Default language is Indonesian
    private const val DEFAULT_LANGUAGE = "English"

    // Available languages
    val LANGUAGE_INDONESIAN = "Indonesia"
    val LANGUAGE_ENGLISH = "English"

    // StateFlow to notify observers when language changes
    private val _currentLanguage = MutableStateFlow(DEFAULT_LANGUAGE)
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    /**
     * Initialize the language manager with saved preferences
     */
    fun initialize(context: Context) {
        val prefs = getPreferences(context)
        val savedLanguage = prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
        _currentLanguage.value = savedLanguage
    }

    /**
     * Change the application language
     */
    fun setLanguage(context: Context, language: String) {
        if (language == _currentLanguage.value) return

        // Save to preferences
        val prefs = getPreferences(context)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()

        // Update state flow
        _currentLanguage.value = language
    }

    /**
     * Get the current application language
     */
    fun getLanguage(context: Context): String {
        val prefs = getPreferences(context)
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    /**
     * Composable function to get localized strings based on current language
     */
    @Composable
    fun getLocalizedStrings(): LocalizedStrings {
        val language by currentLanguage.collectAsState()
        return getLocalizedStrings(language)
    }

    /**
     * Get SharedPreferences instance
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}