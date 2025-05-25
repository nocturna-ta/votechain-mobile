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
 * Singleton class to manage theme settings throughout the app
 */
object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"

    // Theme options
    const val THEME_LIGHT = "Light Mode"
    const val THEME_DARK = "Dark Mode"
    const val THEME_SYSTEM = "Use System Default"

    // Default theme is Light Mode
    private const val DEFAULT_THEME = THEME_LIGHT

    // StateFlow to notify observers when theme changes
    private val _currentTheme = MutableStateFlow(DEFAULT_THEME)
    val currentTheme: StateFlow<String> = _currentTheme.asStateFlow()

    /**
     * Initialize the theme manager with saved preferences
     */
    fun initialize(context: Context) {
        val prefs = getPreferences(context)
        val savedTheme = prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
        _currentTheme.value = savedTheme
    }

    /**
     * Set the application theme
     */
    fun setTheme(context: Context, theme: String) {
        if (theme == _currentTheme.value) return

        // Save to preferences
        val prefs = getPreferences(context)
        prefs.edit().putString(KEY_THEME, theme).apply()

        // Update state flow
        _currentTheme.value = theme
    }

    /**
     * Get the current application theme
     */
    fun getTheme(context: Context): String {
        val prefs = getPreferences(context)
        return prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    }

    /**
     * Composable function to get the current theme
     */
    @Composable
    fun getCurrentTheme(): String {
        val theme by currentTheme.collectAsState()
        return theme
    }

    /**
     * Get SharedPreferences instance
     */
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}