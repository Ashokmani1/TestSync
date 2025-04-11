package com.teksxt.closedtesting.ui.theme


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Define custom shape values for different components in the app
val Shapes = Shapes(
    // Small components like buttons, chips, and small cards
    small = RoundedCornerShape(4.dp),

    // Medium components like medium-sized cards and dialogs
    medium = RoundedCornerShape(8.dp),

    // Large components like large cards and bottom sheets
    large = RoundedCornerShape(16.dp),

    // Extra large components like full-screen dialogs
    extraLarge = RoundedCornerShape(24.dp)
)

// Custom shapes that can be referenced throughout the app
val BottomSheetShape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

val ButtonShape = RoundedCornerShape(12.dp)

val CardShape = RoundedCornerShape(12.dp)

val InputFieldShape = RoundedCornerShape(8.dp)