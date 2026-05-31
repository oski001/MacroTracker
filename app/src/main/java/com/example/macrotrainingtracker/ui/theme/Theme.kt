package com.example.macrotrainingtracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Local provider for corner radius ─────────────────────────────────────────
val LocalCornerRadius = staticCompositionLocalOf { 12.dp }

// ── Helper composable to access corner radius ────────────────────────────────
@Composable
fun cornerRadius() = LocalCornerRadius.current

@Composable
fun MacroTrainingTrackerTheme(content: @Composable () -> Unit) {
    val accent = ThemeManager.accent
    val isDark = ThemeManager.themeConfig.isDarkMode
    val cornerRadius = ThemeManager.cornerRadiusDp

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary          = accent,
            secondary        = accent.copy(alpha = 0.7f),
            tertiary         = Success,
            background       = BackgroundDark,
            surface          = SurfaceDark,
            surfaceVariant   = SurfaceElevated,
            onPrimary        = BackgroundDark,
            onSecondary      = BackgroundDark,
            onBackground     = TextPrimaryDark,
            onSurface        = TextPrimaryDark,
            onSurfaceVariant = TextSecondaryDark,
            outline          = SurfaceBorder,
            error            = Error
        )
    } else {
        lightColorScheme(
            primary          = accent,
            secondary        = accent.copy(alpha = 0.7f),
            tertiary         = Success,
            background       = BackgroundLight,
            surface          = SurfaceLight,
            surfaceVariant   = SurfaceElevatedLight,
            onPrimary        = Color.White,
            onSecondary      = Color.White,
            onBackground     = TextPrimaryLight,
            onSurface        = TextPrimaryLight,
            onSurfaceVariant = TextSecondaryLight,
            outline          = SurfaceBorderLight,
            error            = Error
        )
    }

    CompositionLocalProvider(LocalCornerRadius provides cornerRadius) {
        MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
    }
}
