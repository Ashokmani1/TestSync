package com.teksxt.closedtesting.core.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun getAppIconBackgroundColor(appName: String): Color
{
    // Generate a consistent hash value from the app name
    val hash = appName.fold(0) { acc, char -> acc + char.code }

    // List of pleasant, distinct colors for backgrounds
    val iconColors = listOf(
        Color(0xFF5677FC), // Blue
        Color(0xFF259B24), // Green
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFF3F51B5), // Indigo
        Color(0xFF009688), // Teal
        Color(0xFFFF9800), // Orange
        Color(0xFFE91E63), // Pink
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Blue Grey
    )

    // Use the hash to select a color
    return iconColors[hash % iconColors.size]
}