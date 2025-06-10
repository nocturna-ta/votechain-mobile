package com.nocturna.votechain.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.nocturna.votechain.utils.ThemeManager

private val LightColorScheme = lightColorScheme(
    primary = MainColors.Primary1,
    onPrimary = NeutralColors.Neutral50,
    primaryContainer = PrimaryColors.Primary20,
    onPrimaryContainer = PrimaryColors.Primary90,

    secondary = MainColors.Primary2,
    onSecondary = NeutralColors.Neutral10,
    secondaryContainer = PrimaryColors.Primary30,
    onSecondaryContainer = PrimaryColors.Primary80,

    tertiary = NeutralColors.Neutral60,
    onTertiary = NeutralColors.Neutral10,
    tertiaryContainer = PrimaryColors.Primary20,
    onTertiaryContainer = PrimaryColors.Primary90,

    error = DangerColors.Danger50,
    onError = NeutralColors.Neutral10,
    errorContainer = DangerColors.Danger20,
    onErrorContainer = DangerColors.Danger90,

    background = NeutralColors.Neutral10,
    onBackground = NeutralColors.Neutral40,

    surface = NeutralColors.Neutral10,
    onSurface = PrimaryColors.Primary60,
    surfaceVariant = PrimaryColors.Primary80,
    onSurfaceVariant = PrimaryColors.Primary70,

    outline = NeutralColors.Neutral30,
    outlineVariant = NeutralColors.Neutral20,

    scrim = NeutralColors.Neutral90.copy(alpha = 0.8f),

    inverseSurface = NeutralColors.Neutral70,
    inverseOnSurface = NeutralColors.Neutral10,
    inversePrimary = PrimaryColors.Primary40,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColors.Primary60,
    onPrimary = NeutralColors.Neutral50,
    primaryContainer = PrimaryColors.Primary70,
    onPrimaryContainer = NeutralColors.Neutral10,

    secondary = MainColors.Primary2,
    onSecondary = NeutralColors.Neutral10,
    secondaryContainer = SecondaryColors.Secondary70,
    onSecondaryContainer = NeutralColors.Neutral10,

    tertiary = NeutralColors.Neutral40,
    onTertiary = NeutralColors.Neutral10,
    tertiaryContainer = PrimaryColors.Primary60,
    onTertiaryContainer = NeutralColors.Neutral10,

    error = DangerColors.Danger60,
    onError = NeutralColors.Neutral10,
    errorContainer = DangerColors.Danger70,
    onErrorContainer = NeutralColors.Neutral10,

    background = NeutralColors.Neutral70,
    onBackground = NeutralColors.Neutral40,

    surface = NeutralColors.Neutral70,
    onSurface = NeutralColors.Neutral10,
    surfaceVariant = NeutralColors.Neutral10,
    onSurfaceVariant = NeutralColors.Neutral20,

    outline = NeutralColors.Neutral40,
    outlineVariant = AdditionalColors.strokeColor,

    inverseSurface = NeutralColors.Neutral10,
    inverseOnSurface = NeutralColors.Neutral10,
    inversePrimary = PrimaryColors.Primary40,
)

object StatusBarColors {
    /**
     * Get status bar background color based on theme
     */
    @Composable
    fun getStatusBarColor(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) {
            NeutralColors.Neutral70 // Dark mode: Gray background
        } else {
            NeutralColors.Neutral10 // Light mode: White background
        }
    }

    /**
     * Get status bar icon tint based on theme
     */
    @Composable
    fun getStatusBarIconTint(isDarkTheme: Boolean): Color {
        return if (isDarkTheme) {
            NeutralColors.Neutral10 // Dark mode: White icons
        } else {
            NeutralColors.Neutral80 // Light mode: Dark icons
        }
    }

    /**
     * Check if status bar should use light icons
     */
    fun shouldUseLightStatusBarIcons(isDarkTheme: Boolean): Boolean {
        return !isDarkTheme // Light icons untuk dark theme, dark icons untuk light theme
    }
}

@Composable
fun VotechainTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    val systemInDarkTheme = isSystemInDarkTheme()

    // Determine if dark theme should be used
    val darkTheme = when (currentTheme) {
        ThemeManager.THEME_DARK -> true
        ThemeManager.THEME_LIGHT -> false
        ThemeManager.THEME_SYSTEM -> systemInDarkTheme
        else -> false // Default to light theme
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Get status bar colors
    val statusBarColor = StatusBarColors.getStatusBarColor(darkTheme)
    val statusBarIconTint = StatusBarColors.getStatusBarIconTint(darkTheme)
    val useLightIcons = StatusBarColors.shouldUseLightStatusBarIcons(darkTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Apply custom status bar styling
            window.statusBarColor = statusBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = useLightIcons

            // Navigation bar tetap menggunakan surface color
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Extension functions to help with theme-aware colors
object ThemeColors {
    @Composable
    fun backgroundPrimary() = MaterialTheme.colorScheme.background

    @Composable
    fun backgroundSecondary() = MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun textPrimary() = MaterialTheme.colorScheme.onBackground

    @Composable
    fun textSecondary() = MaterialTheme.colorScheme.onSurfaceVariant

    @Composable
    fun accent() = MaterialTheme.colorScheme.primary

    @Composable
    fun surface() = MaterialTheme.colorScheme.surface

    @Composable
    fun onSurface() = MaterialTheme.colorScheme.onSurface
}