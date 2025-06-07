package com.teksxt.closedtesting.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.teksxt.closedtesting.settings.domain.model.ThemeMode

object ThemeManager {
    @Composable
    fun isDarkTheme(themeMode: ThemeMode): Boolean {
        return when (themeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
        }
    }
}