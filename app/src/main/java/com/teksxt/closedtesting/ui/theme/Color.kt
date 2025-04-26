package com.teksxt.closedtesting.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val LightColorScheme = lightColorScheme(
    // Primary colors - vibrant blue with a modern twist
    primary = Color(0xFF1A73E8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD8E6FF),
    onPrimaryContainer = Color(0xFF001F43),

    // Secondary colors - modern purple
    secondary = Color(0xFF6200EE),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DDFF),
    onSecondaryContainer = Color(0xFF22005D),

    // Tertiary colors - fresh coral
    tertiary = Color(0xFFFF5252),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD9),
    onTertiaryContainer = Color(0xFF410008),

    // Background and surface - cleaner white with subtle tints
    background = Color(0xFFFAFAFC),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceTint = Color(0xFF1A73E8),

    // Variant surfaces - very subtle, modern appearance
    surfaceVariant = Color(0xFFEDF0F7),
    onSurfaceVariant = Color(0xFF42474E),

    // Error colors - bright, attention-grabbing red
    error = Color(0xFFE53935),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Outline colors - subtle but visible
    outline = Color(0xFF8C9198),
    outlineVariant = Color(0xFFCDCFD4),

    // Inverse colors
    inverseSurface = Color(0xFF2F3033),
    inverseOnSurface = Color(0xFFF1F0F4),
    inversePrimary = Color(0xFF9ECAFF)
)

// Contemporary dark color scheme
internal val DarkColorScheme = darkColorScheme(
    // Primary colors - bright but not harsh
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),

    // Secondary colors
    secondary = Color(0xFFBB86FC),
    onSecondary = Color(0xFF361670),
    secondaryContainer = Color(0xFF4D2C88),
    onSecondaryContainer = Color(0xFFF6EAFF),

    // Tertiary colors - vibrant even in dark mode
    tertiary = Color(0xFFFF8A80),
    onTertiary = Color(0xFF681A1C),
    tertiaryContainer = Color(0xFF922022),
    onTertiaryContainer = Color(0xFFFFDAD6),

    // Background and surface - true dark mode, OLED friendly
    background = Color(0xFF111215),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1A1C20),
    onSurface = Color(0xFFE2E2E6),
    surfaceTint = Color(0xFF90CAF9),

    // Variant surfaces
    surfaceVariant = Color(0xFF313236),
    onSurfaceVariant = Color(0xFFDEDFE3),

    // Error colors
    error = Color(0xFFFF8A80),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    // Outline colors
    outline = Color(0xFF9E9E9E),
    outlineVariant = Color(0xFF404145),

    // Inverse colors
    inverseSurface = Color(0xFFE2E2E6),
    inverseOnSurface = Color(0xFF1A1C20),
    inversePrimary = Color(0xFF0069B9)
)