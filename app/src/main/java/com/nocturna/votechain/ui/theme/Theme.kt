package com.nocturna.votechain.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import com.nocturna.votechain.utils.ThemeManager

private val DarkColorScheme = darkColorScheme(
    primary = MainColors.Primary1,
    onPrimary = NeutralColors.Neutral10,
    primaryContainer = PrimaryColors.Primary70,
    onPrimaryContainer = NeutralColors.Neutral10,

    secondary = MainColors.Primary2,
    onSecondary = NeutralColors.Neutral10,
    secondaryContainer = SecondaryColors.Secondary70,
    onSecondaryContainer = NeutralColors.Neutral10,

    tertiary = PrimaryColors.Primary40,
    onTertiary = NeutralColors.Neutral10,
    tertiaryContainer = PrimaryColors.Primary60,
    onTertiaryContainer = NeutralColors.Neutral10,

    error = DangerColors.Danger60,
    onError = NeutralColors.Neutral10,
    errorContainer = DangerColors.Danger70,
    onErrorContainer = NeutralColors.Neutral10,

    background = NeutralColors.Neutral10,
    onBackground = NeutralColors.Neutral90,
    surface = NeutralColors.Neutral20,
    onSurface = NeutralColors.Neutral90,

    surfaceVariant = NeutralColors.Neutral20,
    onSurfaceVariant = NeutralColors.Neutral80,
    outline = NeutralColors.Neutral70
)

private val LightColorScheme = lightColorScheme(
    primary = MainColors.Primary1,
    onPrimary = NeutralColors.Neutral10,
    primaryContainer = PrimaryColors.Primary30,
    onPrimaryContainer = PrimaryColors.Primary90,

    secondary = MainColors.Primary2,
    onSecondary = NeutralColors.Neutral10,
    secondaryContainer = SecondaryColors.Secondary30,
    onSecondaryContainer = SecondaryColors.Secondary90,

    tertiary = PrimaryColors.Primary40,
    onTertiary = NeutralColors.Neutral10,
    tertiaryContainer = PrimaryColors.Primary20,
    onTertiaryContainer = PrimaryColors.Primary80,

    error = DangerColors.Danger50,
    onError = NeutralColors.Neutral10,
    errorContainer = DangerColors.Danger20,
    onErrorContainer = DangerColors.Danger90,

    background = NeutralColors.Neutral10,
    onBackground = NeutralColors.Neutral90,
    surface = NeutralColors.Neutral20,
    onSurface = NeutralColors.Neutral90,

    surfaceVariant = NeutralColors.Neutral20,
    onSurfaceVariant = NeutralColors.Neutral70,
    outline = NeutralColors.Neutral60
)

object ExtendedColors {
    val success: Color
        @Composable
        get() = if (isSystemInDarkTheme()) SuccessColors.Success60 else SuccessColors.Success50

    val warning: Color
        @Composable
        get() = if (isSystemInDarkTheme()) WarningColors.Warning60 else WarningColors.Warning50

    val info: Color
        @Composable
        get() = if (isSystemInDarkTheme()) InfoColors.Info60 else InfoColors.Info50

    val onSuccess: Color
        @Composable
        get() = NeutralColors.Neutral10

    val onWarning: Color
        @Composable
        get() = NeutralColors.Neutral90

    val onInfo: Color
        @Composable
        get() = NeutralColors.Neutral10

    // Container colors
    val successContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) SuccessColors.Success70 else SuccessColors.Success20

    val warningContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) WarningColors.Warning70 else WarningColors.Warning20

    val infoContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) InfoColors.Info70 else InfoColors.Info20

    val onSuccessContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) SuccessColors.Success10 else SuccessColors.Success90

    val onWarningContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) WarningColors.Warning10 else WarningColors.Warning90

    val onInfoContainer: Color
        @Composable
        get() = if (isSystemInDarkTheme()) InfoColors.Info10 else InfoColors.Info90
}

@Composable
fun VotechainTheme(
    content: @Composable () -> Unit
) {
    // Get current theme from ThemeManager
    val themeState = ThemeManager.currentTheme.collectAsState().value

    // Determine whether to use dark theme based on the selected theme
    val useDarkTheme = when (themeState) {
        ThemeManager.THEME_LIGHT -> false
        ThemeManager.THEME_DARK -> true
        ThemeManager.THEME_SYSTEM -> isSystemInDarkTheme()
        else -> false // Default to light theme
    }

    val colorScheme = if (useDarkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

object ThemeExtended {
    val colors: ExtendedColors
        @Composable
        get() = ExtendedColors
}