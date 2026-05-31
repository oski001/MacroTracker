package com.example.macrotrainingtracker.ui.theme

import androidx.compose.ui.graphics.Color

// Dark theme
val BackgroundDark    = Color(0xFF0A0A0A)
val SurfaceDark       = Color(0xFF121212)
val SurfaceElevated   = Color(0xFF1A1A1A)
val SurfaceBorder     = Color(0xFF2A2A2A)
val TextPrimaryDark   = Color(0xFFFFFFFF)
val TextSecondaryDark = Color(0xFF9E9E9E)

// Light theme
val BackgroundLight    = Color(0xFFF5F5F5)
val SurfaceLight       = Color(0xFFFFFFFF)
val SurfaceElevatedLight = Color(0xFFFAFAFA)
val SurfaceBorderLight = Color(0xFFE0E0E0)
val TextPrimaryLight   = Color(0xFF212121)
val TextSecondaryLight = Color(0xFF757575)

// Dynamic colors — backed by ThemeManager state; composables auto-recompose
val AccentPrimary: Color get() = ThemeManager.accent
val AccentSecondary: Color get() = ThemeManager.accent.copy(alpha = 0.7f)

// Status colors (same for dark/light)
val Success = Color(0xFF00E676)
val Warning = Color(0xFFFFAB00)
val Error   = Color(0xFFFF5252)

