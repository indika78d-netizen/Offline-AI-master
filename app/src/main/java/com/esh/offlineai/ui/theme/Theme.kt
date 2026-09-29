package com.esh.offlineai.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * MindSync v1.2.0 — Liquid Skin Theme
 *
 * Deep oceanic dark scheme with luminous cyan accents and
 * fluid glassmorphism aesthetics throughout the app.
 */
private val LiquidDarkColorScheme = darkColorScheme(
    primary = LiquidElectricBlue,
    onPrimary = LiquidTextPrimary,
    primaryContainer = LiquidMidBlue,
    onPrimaryContainer = LiquidCyanLight,

    secondary = LiquidCyan,
    onSecondary = LiquidDeepBlue,
    secondaryContainer = LiquidDarkBlue,
    onSecondaryContainer = LiquidCyan,

    tertiary = LiquidPurple,
    onTertiary = LiquidTextPrimary,
    tertiaryContainer = LiquidMidBlue,
    onTertiaryContainer = LiquidPurpleLight,

    error = LiquidError,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = LiquidDeepBlue,
    onBackground = LiquidTextPrimary,

    surface = LiquidSurface1,
    onSurface = LiquidTextPrimary,
    surfaceVariant = LiquidSurface2,
    onSurfaceVariant = LiquidTextSecondary,

    outline = LiquidGlassBorder,
    outlineVariant = Color(0xFF1E2F55),

    inverseSurface = LiquidTextPrimary,
    inverseOnSurface = LiquidDeepBlue,
    inversePrimary = LiquidElectricBlue,

    surfaceTint = LiquidCyan
)

@Composable
fun OfflineAITheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LiquidDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar blend into the deep liquid background
            window.statusBarColor = LiquidDeepBlue.toArgb()
            window.navigationBarColor = LiquidDeepBlue.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}